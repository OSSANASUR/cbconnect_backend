package com.ossanasur.cbconnect.module.sinistre.service.impl;

import com.ossanasur.cbconnect.common.enums.*;
import com.ossanasur.cbconnect.exception.*;
import com.ossanasur.cbconnect.historique.SinistreVersioningService;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.module.auth.repository.OrganismeRepository;
import com.ossanasur.cbconnect.module.auth.repository.UtilisateurRepository;
import com.ossanasur.cbconnect.module.pays.entity.Pays;
import com.ossanasur.cbconnect.module.pays.repository.PaysRepository;
import com.ossanasur.cbconnect.module.sinistre.dto.request.SinistreRequest;
import com.ossanasur.cbconnect.module.sinistre.dto.response.SinistreResponse;
import com.ossanasur.cbconnect.module.sinistre.entity.*;
import com.ossanasur.cbconnect.module.sinistre.mapper.SinistreMapper;
import com.ossanasur.cbconnect.module.sinistre.repository.*;
import com.ossanasur.cbconnect.module.sinistre.service.SinistreService;
import com.ossanasur.cbconnect.utils.DataResponse;
import com.ossanasur.cbconnect.utils.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SinistreServiceImpl implements SinistreService {
    private final SinistreRepository sinistreRepository;
    private final AssureRepository assureRepository;
    private final PaysRepository paysRepository;
    private final OrganismeRepository organismeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final SinistreVersioningService versioningService;
    private final SinistreMapper sinistreMapper;

    @Override
    @Transactional
    public DataResponse<SinistreResponse> create(SinistreRequest r, String loginAuteur) {
        Pays paysG = paysRepository.findActiveByTrackingId(r.paysGestionnaireTrackingId())
                .orElseThrow(() -> new RessourceNotFoundException("Pays gestionnaire introuvable"));
        Assure assure = assureRepository.findActiveByTrackingId(r.assureTrackingId())
                .orElseThrow(() -> new RessourceNotFoundException("Assure introuvable"));

        // Generation du numero local
        int annee = LocalDate.now().getYear();
        long seq = sinistreRepository.countByAnneeAndType(annee, r.typeSinistre()) + 1;
        String codeType = r.typeDommage() == TypeDommage.CORPOREL ? "C"
                : r.typeDommage() == TypeDommage.MATERIEL ? "M" : "X";
        String numeroLocal;
        if (r.typeSinistre() == TypeSinistre.SURVENU_TOGO) {
            String codePays = paysG.getCodeCarteBrune() != null ? paysG.getCodeCarteBrune() : "TG";
            if (r.paysEmetteurTrackingId() != null) {
                Pays paysE = paysRepository.findActiveByTrackingId(r.paysEmetteurTrackingId()).orElse(null);
                codePays = paysE != null ? paysE.getCodeCarteBrune() : codePays;
            }
            numeroLocal = codePays + "-" + codeType + String.format("%04d", seq) + "/" + annee;
        } else {
            String codeCie = assure.getOrganisme() != null ? assure.getOrganisme().getCode() : "BNCB";
            String codePaysE = "";
            if (r.paysEmetteurTrackingId() != null) {
                Pays paysE = paysRepository.findActiveByTrackingId(r.paysEmetteurTrackingId()).orElse(null);
                codePaysE = paysE != null ? "-" + paysE.getCodeCarteBrune() : "";
            }
            numeroLocal = codeCie + codePaysE + "-" + codeType + String.format("%04d", seq) + "/" + annee;
        }

        Sinistre s = Sinistre.builder()
                .sinistreTrackingId(UUID.randomUUID()).numeroSinistreLocal(numeroLocal)
                .numeroSinistreManuel(r.numeroSinistreManuel()).numeroSinistreHomologue(r.numeroSinistreHomologue())
                .numeroSinistreEcarteBrune(r.numeroSinistreEcarteBrune())
                .typeSinistre(r.typeSinistre()).typeDommage(r.typeDommage()).statut(StatutSinistre.NOUVEAU)
                .dateAccident(r.dateAccident())
                .dateDeclaration(r.dateDeclaration() != null ? r.dateDeclaration() : LocalDate.now())
                .lieuAccident(r.lieuAccident()).agglomeration(r.agglomeration())
                .paysGestionnaire(paysG).assure(assure)
                .createdBy(loginAuteur).activeData(true).deletedData(false).fromTable(TypeTable.SINISTRE)
                .build();

        if (r.paysEmetteurTrackingId() != null)
            paysRepository.findActiveByTrackingId(r.paysEmetteurTrackingId()).ifPresent(s::setPaysEmetteur);
        if (r.organismeMembreTrackingId() != null)
            organismeRepository.findActiveByTrackingId(r.organismeMembreTrackingId()).ifPresent(s::setOrganismeMembre);
        if (r.organismeHomologueTrackingId() != null)
            organismeRepository.findActiveByTrackingId(r.organismeHomologueTrackingId())
                    .ifPresent(s::setOrganismeHomologue);
        if (r.redacteurTrackingId() != null)
            utilisateurRepository
                    .findByUtilisateurTrackingIdAndActiveDataTrueAndDeletedDataFalse(r.redacteurTrackingId())
                    .ifPresent(s::setRedacteur);

        return DataResponse.created("Sinistre cree : " + numeroLocal,
                sinistreMapper.toResponse(sinistreRepository.save(s)));
    }

    @Override
    @Transactional
    public DataResponse<SinistreResponse> update(UUID id, SinistreRequest r, String loginAuteur) {
        Sinistre updated = versioningService.createVersion(id, r, loginAuteur);
        return DataResponse.success("Sinistre mis a jour", sinistreMapper.toResponse(updated));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<SinistreResponse> getByTrackingId(UUID id) {
        return DataResponse.success(sinistreMapper.toResponse(versioningService.getActiveVersion(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<SinistreResponse> getAll(int page, int size) {
        return PaginatedResponse.fromPage(sinistreRepository.findAllActive(
                PageRequest.of(page, size, Sort.by("dateDeclaration").descending()))
                .map(sinistreMapper::toResponse), "Liste des sinistres");
    }

    @Override
    @Transactional
    public DataResponse<Void> changerStatut(UUID id, String nouveauStatut, String loginAuteur) {
        Sinistre s = versioningService.getActiveVersion(id);
        StatutSinistre statut;
        try {
            statut = StatutSinistre.valueOf(nouveauStatut.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Statut invalide : " + nouveauStatut);
        }
        s.setStatut(statut);
        s.setUpdatedBy(loginAuteur);
        sinistreRepository.save(s);
        return DataResponse.success("Statut mis a jour : " + statut, null);
    }

    @Override
    @Transactional
    public DataResponse<Void> assignerRedacteur(UUID sinistreId, UUID redacteurId, String loginAuteur) {
        Sinistre s = versioningService.getActiveVersion(sinistreId);
        Utilisateur r = utilisateurRepository
                .findByUtilisateurTrackingIdAndActiveDataTrueAndDeletedDataFalse(redacteurId)
                .orElseThrow(() -> new RessourceNotFoundException("Redacteur introuvable"));
        s.setRedacteur(r);
        s.setUpdatedBy(loginAuteur);
        sinistreRepository.save(s);
        return DataResponse.success("Redacteur assigne", null);
    }

    @Override
    @Transactional
    public DataResponse<Void> delete(UUID id, String loginAuteur) {
        versioningService.softDelete(id, loginAuteur);
        return DataResponse.success("Sinistre supprime", null);
    }
}
