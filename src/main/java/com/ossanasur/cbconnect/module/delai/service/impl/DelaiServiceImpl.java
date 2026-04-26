package com.ossanasur.cbconnect.module.delai.service.impl;

import com.ossanasur.cbconnect.common.enums.*;
import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.module.auth.repository.UtilisateurRepository;
import com.ossanasur.cbconnect.module.delai.dto.request.ParametreDelaiUpdateRequest;
import com.ossanasur.cbconnect.module.delai.dto.request.ParametreSystemeUpdateRequest;
import com.ossanasur.cbconnect.module.delai.dto.response.*;
import com.ossanasur.cbconnect.module.delai.entity.*;
import com.ossanasur.cbconnect.module.delai.mapper.DelaiMapper;
import com.ossanasur.cbconnect.module.delai.repository.*;
import com.ossanasur.cbconnect.module.delai.service.DelaiService;
import com.ossanasur.cbconnect.module.sinistre.repository.SinistreRepository;
import com.ossanasur.cbconnect.security.service.EmailSenderService;
import com.ossanasur.cbconnect.utils.DataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j @Service @RequiredArgsConstructor
public class DelaiServiceImpl implements DelaiService {

    private final ParametreDelaiRepository parametreDelaiRepository;
    private final ParametreSystemeRepository parametreSystemeRepository;
    private final NotificationDelaiRepository notificationRepository;
    private final SinistreRepository sinistreRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EmailSenderService emailSenderService;
    private final DelaiMapper delaiMapper;

    private static final DateTimeFormatter DATE_FR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Map<NiveauAlerteDelai, String> NIVEAU_COULEUR = Map.of(
        NiveauAlerteDelai.ATTENTION, "#d5c93b",
        NiveauAlerteDelai.URGENT,    "#f97316",
        NiveauAlerteDelai.CRITIQUE,  "#ef4444"
    );
    private static final Map<NiveauAlerteDelai, String> NIVEAU_BG = Map.of(
        NiveauAlerteDelai.ATTENTION, "#fef9c3",
        NiveauAlerteDelai.URGENT,    "#ffedd5",
        NiveauAlerteDelai.CRITIQUE,  "#fee2e2"
    );
    private static final Map<NiveauAlerteDelai, String> NIVEAU_LABEL = Map.of(
        NiveauAlerteDelai.ATTENTION, "Alerte ATTENTION — 50% du délai écoulé",
        NiveauAlerteDelai.URGENT,    "Alerte URGENTE — 75% du délai écoulé",
        NiveauAlerteDelai.CRITIQUE,  "CRITIQUE — Délai atteint ou dépassé"
    );
    private static final Map<NiveauAlerteDelai, String> NIVEAU_SUBJECT = Map.of(
        NiveauAlerteDelai.ATTENTION, "[CBConnect] Alerte ATTENTION — Délai à 50%%",
        NiveauAlerteDelai.URGENT,    "[CBConnect] ⚠ Alerte URGENTE — Délai à 75%%",
        NiveauAlerteDelai.CRITIQUE,  "[CBConnect] 🔴 CRITIQUE — Délai dépassé"
    );

    @Override @Transactional
    public void initialiserDelaisPourSinistre(UUID sinistreId, String loginAuteur) {
        var sinistre = sinistreRepository.findActiveByTrackingId(sinistreId)
            .orElseThrow(() -> new RessourceNotFoundException("Sinistre introuvable"));
        List<ParametreDelai> delais = parametreDelaiRepository.findAllActiveByType(
            java.util.List.of(sinistre.getTypeSinistre(), TypeSinistre.TOUS));
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
            if (!ancien.equals(nouveau) && !NiveauAlerteDelai.NORMAL.equals(nouveau)) {
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
                envoyerAlerteMail(n, nouveau);
                mises++;
            }
        }
        log.info("Recalcul alertes délais : {} niveaux mis à jour, emails envoyés", mises);
    }

    private void envoyerAlerteMail(NotificationDelai n, NiveauAlerteDelai niveau) {
        String sinistreNum = n.getSinistre() != null ? n.getSinistre().getNumeroSinistreLocal() : "—";
        long joursRestants = ChronoUnit.DAYS.between(LocalDate.now(), n.getDateEcheance());

        Map<String, Object> vars = new HashMap<>();
        vars.put("niveauAlerte",     niveau.name());
        vars.put("niveauLabel",      NIVEAU_LABEL.getOrDefault(niveau, niveau.name()));
        vars.put("niveauCouleur",    NIVEAU_COULEUR.getOrDefault(niveau, "#1a6b3c"));
        vars.put("niveauBg",         NIVEAU_BG.getOrDefault(niveau, "#e8f5ed"));
        vars.put("libelleDelai",     n.getParametreDelai() != null ? n.getParametreDelai().getLibelle() : "—");
        vars.put("referenceJuridique", n.getParametreDelai() != null ? n.getParametreDelai().getReferenceJuridique() : "");
        vars.put("sinistreNumero",   sinistreNum);
        vars.put("dateEcheance",     n.getDateEcheance().format(DATE_FR));
        vars.put("joursRestants",    joursRestants);
        vars.put("lienDossier",      "#");

        String subject = NIVEAU_SUBJECT.getOrDefault(niveau, "[CBConnect] Alerte délai")
            + " — " + sinistreNum;

        Set<String> destinataires = new HashSet<>();

        // Rédacteur responsable du dossier
        if (n.getResponsable() != null && n.getResponsable().getEmail() != null) {
            destinataires.add(n.getResponsable().getEmail());
        } else if (n.getSinistre() != null && n.getSinistre().getCreatedBy() != null) {
            // createdBy est le login/email du créateur
            destinataires.add(n.getSinistre().getCreatedBy());
        }

        // URGENT + CRITIQUE → on ajoute les CSS
        if (NiveauAlerteDelai.URGENT.equals(niveau) || NiveauAlerteDelai.CRITIQUE.equals(niveau)) {
            utilisateurRepository.findActiveByProfil("CSS")
                .forEach(u -> { if (u.getEmail() != null) destinataires.add(u.getEmail()); });
        }

        // CRITIQUE → on ajoute aussi les SE
        if (NiveauAlerteDelai.CRITIQUE.equals(niveau)) {
            utilisateurRepository.findActiveByProfil("SE")
                .forEach(u -> { if (u.getEmail() != null) destinataires.add(u.getEmail()); });
        }

        for (String email : destinataires) {
            try {
                emailSenderService.sendTemplated(email, subject, "alerte_delai", vars);
            } catch (Exception ex) {
                log.warn("Échec envoi alerte mail à {} pour sinistre {} : {}", email, sinistreNum, ex.getMessage());
            }
        }
    }

    @Override @Transactional
    public DataResponse<Void> relancerManuellement(Integer id) {
        NotificationDelai n = notificationRepository.findById(id)
            .orElseThrow(() -> new RessourceNotFoundException("Notification introuvable"));
        envoyerAlerteMail(n, n.getNiveauAlerte());
        n.setNombreAlertes(n.getNombreAlertes() + 1);
        n.setDerniereAlerteEnvoyee(LocalDateTime.now());
        notificationRepository.save(n);
        return DataResponse.success("Relance envoyée", null);
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
