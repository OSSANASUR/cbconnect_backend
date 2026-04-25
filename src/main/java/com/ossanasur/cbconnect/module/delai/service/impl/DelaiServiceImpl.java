package com.ossanasur.cbconnect.module.delai.service.impl;

import com.ossanasur.cbconnect.common.enums.*;
import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.module.delai.dto.request.ParametreDelaiUpdateRequest;
import com.ossanasur.cbconnect.module.delai.dto.request.ParametreSystemeUpdateRequest;
import com.ossanasur.cbconnect.module.delai.dto.response.*;
import com.ossanasur.cbconnect.module.delai.entity.*;
import com.ossanasur.cbconnect.module.delai.mapper.DelaiMapper;
import com.ossanasur.cbconnect.module.delai.repository.*;
import com.ossanasur.cbconnect.module.delai.service.DelaiService;
import com.ossanasur.cbconnect.module.sinistre.repository.SinistreRepository;
import com.ossanasur.cbconnect.utils.DataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j @Service @RequiredArgsConstructor
public class DelaiServiceImpl implements DelaiService {

    private final ParametreDelaiRepository parametreDelaiRepository;
    private final ParametreSystemeRepository parametreSystemeRepository;
    private final NotificationDelaiRepository notificationRepository;
    private final SinistreRepository sinistreRepository;
    private final DelaiMapper delaiMapper;

    @Override @Transactional
    public void initialiserDelaisPourSinistre(UUID sinistreId, String loginAuteur) {
        var sinistre = sinistreRepository.findActiveByTrackingId(sinistreId)
            .orElseThrow(() -> new RessourceNotFoundException("Sinistre introuvable"));
        List<ParametreDelai> delais = parametreDelaiRepository.findAllActiveByType(sinistre.getTypeSinistre());
        for (ParametreDelai delai : delais) {
            if (TypeDelai.DELAI_MAX.equals(delai.getTypeDelai())) {
                LocalDate echeance = calculerEcheance(LocalDate.now(), delai);
                NotificationDelai notif = NotificationDelai.builder()
                    .parametreDelai(delai).sinistre(sinistre).dateDebut(LocalDate.now())
                    .dateEcheance(echeance).statut(StatutNotificationDelai.EN_COURS)
                    .niveauAlerte(NiveauAlerteDelai.NORMAL).nombreAlertes(0).build();
                notificationRepository.save(notif);
            }
        }
        log.info("Délais initialisés pour sinistre {} : {} délais MAX suivis",
            sinistre.getNumeroSinistreLocal(), delais.stream().filter(d -> TypeDelai.DELAI_MAX.equals(d.getTypeDelai())).count());
    }

    @Override @Transactional
    public void recalculerAlertes() {
        List<NotificationDelai> actives = notificationRepository.findEchus(LocalDate.now().plusDays(30));
        int mises = 0;
        for (NotificationDelai n : actives) {
            NiveauAlerteDelai ancien = n.getNiveauAlerte();
            NiveauAlerteDelai nouveau = calculerNiveau(n);
            if (!ancien.equals(nouveau)) {
                n.setNiveauAlerte(nouveau);
                if (NiveauAlerteDelai.CRITIQUE.equals(nouveau)) {
                    n.setStatut(StatutNotificationDelai.DEPASSE);
                } else if (NiveauAlerteDelai.URGENT.equals(nouveau)) {
                    n.setStatut(StatutNotificationDelai.ALERTE_3);
                } else if (NiveauAlerteDelai.ATTENTION.equals(nouveau)) {
                    n.setStatut(StatutNotificationDelai.ALERTE_2);
                }
                n.setNombreAlertes(n.getNombreAlertes() + 1);
                n.setDerniereAlerteEnvoyee(LocalDateTime.now());
                notificationRepository.save(n);
                mises++;
            }
        }
        log.info("Recalcul alertes délais : {} niveaux mis à jour", mises);
    }

    @Override @Transactional
    public DataResponse<Void> resoudre(Integer id, String motif, String loginAuteur) {
        var notif = notificationRepository.findById(id)
            .orElseThrow(() -> new RessourceNotFoundException("Notification introuvable"));
        notif.setStatut(StatutNotificationDelai.RESOLU);
        notif.setDateResolution(LocalDateTime.now());
        notif.setMotifResolution(motif);
        notificationRepository.save(notif);
        return DataResponse.success("Délai marqué résolu", null);
    }

    @Override @Transactional(readOnly = true)
    public DataResponse<List<NotificationDelaiResponse>> getActiveBySinistre(UUID sinistreId) {
        return DataResponse.success(notificationRepository.findActiveBySinistre(sinistreId)
            .stream().map(delaiMapper::toResponse).collect(Collectors.toList()));
    }

    @Override @Transactional(readOnly = true)
    public DataResponse<List<NotificationDelaiResponse>> getMesAlertes(String loginEmail) {
        return DataResponse.success(List.of());
    }

    @Override @Transactional(readOnly = true)
    public DataResponse<List<NotificationDelaiResponse>> getUrgents() {
        return DataResponse.success(notificationRepository.findUrgents()
            .stream().map(delaiMapper::toResponse).collect(Collectors.toList()));
    }

    // ─── Référentiel délais ───────────────────────────────────────────────────

    @Override @Transactional(readOnly = true)
    public DataResponse<List<ParametreDelaiResponse>> listParametres() {
        return DataResponse.success(parametreDelaiRepository.findAllOrdered()
            .stream().map(delaiMapper::toParametreResponse).toList());
    }

    @Override @Transactional
    public DataResponse<ParametreDelaiResponse> updateParametre(Integer id, ParametreDelaiUpdateRequest r) {
        ParametreDelai p = parametreDelaiRepository.findById(id)
            .orElseThrow(() -> new RessourceNotFoundException("Paramètre délai introuvable : " + id));
        if (!p.isModifiable())
            throw new IllegalStateException("Ce délai n'est pas modifiable");
        if (r.valeur() != null) p.setValeur(r.valeur());
        if (r.seuilAlerte1Pct() != null) p.setSeuilAlerte1Pct(r.seuilAlerte1Pct());
        if (r.seuilAlerte2Pct() != null) p.setSeuilAlerte2Pct(r.seuilAlerte2Pct());
        if (r.tauxPenalitePct() != null) p.setTauxPenalitePct(r.tauxPenalitePct());
        if (r.actif() != null) p.setActif(r.actif());
        return DataResponse.success("Paramètre mis à jour", delaiMapper.toParametreResponse(parametreDelaiRepository.save(p)));
    }

    // ─── Paramètres système ───────────────────────────────────────────────────

    @Override @Transactional(readOnly = true)
    public DataResponse<List<ParametreSystemeResponse>> listParametresSysteme() {
        return DataResponse.success(parametreSystemeRepository.findAllByActifTrueOrderByCle()
            .stream().map(delaiMapper::toSystemeResponse).toList());
    }

    @Override @Transactional
    public DataResponse<ParametreSystemeResponse> updateParametreSysteme(Integer id, ParametreSystemeUpdateRequest r) {
        ParametreSysteme s = parametreSystemeRepository.findById(id)
            .orElseThrow(() -> new RessourceNotFoundException("Paramètre système introuvable : " + id));
        if (r.valeurDecimal() != null) s.setValeurDecimal(r.valeurDecimal());
        if (r.actif() != null) s.setActif(r.actif());
        return DataResponse.success("Paramètre mis à jour", delaiMapper.toSystemeResponse(parametreSystemeRepository.save(s)));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private LocalDate calculerEcheance(LocalDate debut, ParametreDelai delai) {
        BigDecimal valeur = delai.getValeur();
        return switch (delai.getUnite()) {
            case JOURS_CALENDAIRES, JOURS_OUVRES -> debut.plusDays(valeur.longValue());
            case MOIS -> debut.plusMonths(valeur.longValue());
            case ANS -> debut.plusYears(valeur.longValue());
            default -> debut.plusDays(valeur.longValue());
        };
    }

    private NiveauAlerteDelai calculerNiveau(NotificationDelai n) {
        long totalJours = ChronoUnit.DAYS.between(n.getDateDebut(), n.getDateEcheance());
        long joursEcoules = ChronoUnit.DAYS.between(n.getDateDebut(), LocalDate.now());
        if (totalJours <= 0) return NiveauAlerteDelai.CRITIQUE;
        double pct = (double) joursEcoules / totalJours;
        ParametreDelai p = n.getParametreDelai();
        double s1 = p.getSeuilAlerte1Pct() != null ? p.getSeuilAlerte1Pct().doubleValue() / 100.0 : 0.50;
        double s2 = p.getSeuilAlerte2Pct() != null ? p.getSeuilAlerte2Pct().doubleValue() / 100.0 : 0.75;
        if (pct >= 1.0) return NiveauAlerteDelai.CRITIQUE;
        if (pct >= s2)  return NiveauAlerteDelai.URGENT;
        if (pct >= s1)  return NiveauAlerteDelai.ATTENTION;
        return NiveauAlerteDelai.NORMAL;
    }
}
