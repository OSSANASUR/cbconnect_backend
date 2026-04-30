package com.ossanasur.cbconnect.module.courrier.service.impl;

import com.ossanasur.cbconnect.common.enums.CanalCourrier;
import com.ossanasur.cbconnect.common.enums.TypeCourrier;
import com.ossanasur.cbconnect.common.enums.TypeRegistre;
import com.ossanasur.cbconnect.common.enums.TypeTable;
import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.module.auth.repository.OrganismeRepository;
import com.ossanasur.cbconnect.module.auth.repository.UtilisateurRepository;
import com.ossanasur.cbconnect.module.courrier.dto.request.CourrierRequest;
import com.ossanasur.cbconnect.module.courrier.dto.request.DestinataireInterneRequest;
import com.ossanasur.cbconnect.module.courrier.dto.response.CourrierResponse;
import com.ossanasur.cbconnect.module.courrier.entity.Courrier;
import com.ossanasur.cbconnect.module.courrier.entity.CourrierDestinataireInterne;
import com.ossanasur.cbconnect.module.courrier.entity.RegistreJour;
import com.ossanasur.cbconnect.module.courrier.mapper.CourrierMapper;
import com.ossanasur.cbconnect.module.courrier.repository.CourrierRepository;
import com.ossanasur.cbconnect.module.courrier.repository.RegistreJourRepository;
import com.ossanasur.cbconnect.module.courrier.service.CourrierService;
import com.ossanasur.cbconnect.module.courrier.service.RegistreJourService;
import com.ossanasur.cbconnect.module.sinistre.repository.SinistreRepository;
import com.ossanasur.cbconnect.utils.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourrierServiceImpl implements CourrierService {

    private final CourrierRepository courrierRepository;
    private final SinistreRepository sinistreRepository;
    private final OrganismeRepository organismeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final RegistreJourRepository registreRepository;
    private final RegistreJourService registreService;
    private final CourrierMapper mapper;

    @Override
    @Transactional
    public DataResponse<CourrierResponse> enregistrer(CourrierRequest r, String loginAuteur) {

        // Numérotation auto si non fournie : LKS/AAAA/NNNNN (sortant) ou ENT/AAAA/NNNNN (entrant)
        String ref = r.referenceCourrier();
        if (ref == null || ref.isBlank()) {
            long seq = courrierRepository.count() + 1;
            String sens = TypeCourrier.SORTANT.equals(r.typeCourrier()) ? "LKS" : "ENT";
            ref = sens + "/" + LocalDate.now().getYear() + "/" + String.format("%05d", seq);
        }

        Courrier c = Courrier.builder()
            .courrierTrackingId(UUID.randomUUID())
            .referenceCourrier(ref)
            .typeCourrier(r.typeCourrier())
            .nature(r.nature())
            .expediteur(r.expediteur())
            .destinataire(r.destinataire())
            .objet(r.objet())
            .dateCourrier(r.dateCourrier())
            .dateReception(r.dateReception())
            .canal(r.canal() != null ? r.canal() : CanalCourrier.MAIL)
            .referenceBordereau(r.referenceBordereau())
            .traite(false)
            .numeroSinistreHomologueRef(r.numeroSinistreHomologueRef())
            .serviceDestinataireInterne(r.serviceDestinataireInterne())
            .createdBy(loginAuteur).activeData(true).deletedData(false)
            .fromTable(TypeTable.COURRIER)
            .build();

        // Sinistre lié (optionnel)
        if (r.sinistreTrackingId() != null) {
            sinistreRepository.findActiveByTrackingId(r.sinistreTrackingId()).ifPresent(c::setSinistre);
        }

        // Destinataire structuré (bureau homologue) — optionnel
        if (r.destinataireOrganismeTrackingId() != null) {
            organismeRepository.findActiveByTrackingId(r.destinataireOrganismeTrackingId())
                .ifPresent(c::setDestinataireOrganisme);
        }

        // Rattachement registre : explicite OU auto-rattachement à OUVERT du jour
        RegistreJour registre = null;
        if (r.registreJourTrackingId() != null) {
            registre = registreRepository.findActiveByTrackingId(r.registreJourTrackingId())
                .orElseThrow(() -> new RessourceNotFoundException("Registre introuvable"));
        } else {
            TypeRegistre type = TypeCourrier.ENTRANT.equals(r.typeCourrier())
                ? TypeRegistre.ARRIVEE : TypeRegistre.DEPART;
            registre = registreService.getOuCreerRegistreOuvert(LocalDate.now(), type, loginAuteur);
        }
        c.setRegistreJour(registre);

        // Persister avant de rattacher les destinataires internes (besoin de l'ID)
        Courrier saved = courrierRepository.save(c);

        // Dispatch multi-destinataires internes
        if (r.destinatairesInternes() != null && !r.destinatairesInternes().isEmpty()) {
            List<CourrierDestinataireInterne> dests = new ArrayList<>();
            for (DestinataireInterneRequest d : r.destinatairesInternes()) {
                var builder = CourrierDestinataireInterne.builder()
                    .courrier(saved)
                    .serviceLibelle(d.serviceLibelle())
                    .observations(d.observations())
                    .dateRemiseInterne(LocalDateTime.now())
                    .createdBy(loginAuteur)
                    .createdAt(LocalDateTime.now());
                if (d.utilisateurTrackingId() != null) {
                    utilisateurRepository
                        .findByUtilisateurTrackingIdAndActiveDataTrueAndDeletedDataFalse(d.utilisateurTrackingId())
                        .ifPresent(builder::utilisateur);
                }
                dests.add(builder.build());
            }
            saved.getDestinatairesInternes().addAll(dests);
            saved = courrierRepository.save(saved);
        }

        return DataResponse.created("Courrier enregistré : " + ref, mapper.toResponse(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<CourrierResponse> getByTrackingId(UUID id) {
        return DataResponse.success(mapper.toResponse(courrierRepository.findActiveByTrackingId(id)
            .orElseThrow(() -> new RessourceNotFoundException("Courrier introuvable"))));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<CourrierResponse>> getBySinistre(UUID sinistreId) {
        return DataResponse.success(courrierRepository.findBySinistre(sinistreId).stream()
            .map(mapper::toResponse).collect(Collectors.toList()));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<CourrierResponse>> getNonTraites() {
        return DataResponse.success(courrierRepository.findNonTraites().stream()
            .map(mapper::toResponse).collect(Collectors.toList()));
    }

    @Override
    @Transactional
    public DataResponse<Void> marquerTraite(UUID id, String loginAuteur) {
        Courrier c = courrierRepository.findActiveByTrackingId(id)
            .orElseThrow(() -> new RessourceNotFoundException("Courrier introuvable"));
        c.setTraite(true);
        c.setDateTraitement(LocalDateTime.now());
        c.setUpdatedBy(loginAuteur);
        courrierRepository.save(c);
        return DataResponse.success("Courrier marqué comme traité", null);
    }

    @Override
    @Transactional
    public DataResponse<Void> supprimer(UUID id, String loginAuteur) {
        Courrier c = courrierRepository.findActiveByTrackingId(id)
            .orElseThrow(() -> new RessourceNotFoundException("Courrier introuvable"));
        c.setActiveData(false);
        c.setDeletedData(true);
        c.setDeletedAt(LocalDateTime.now());
        c.setDeletedBy(loginAuteur);
        courrierRepository.save(c);
        return DataResponse.success("Courrier supprimé", null);
    }
}
