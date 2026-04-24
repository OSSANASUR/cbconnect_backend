package com.ossanasur.cbconnect.module.finance.service.impl;

import com.ossanasur.cbconnect.common.enums.StatutEcritureComptable;
import com.ossanasur.cbconnect.common.enums.StatutPaiement;
import com.ossanasur.cbconnect.common.enums.TypeTransactionComptable;
import com.ossanasur.cbconnect.exception.BadRequestException;
import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.module.auth.entity.Organisme;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.module.auth.repository.OrganismeRepository;
import com.ossanasur.cbconnect.module.auth.repository.UtilisateurRepository;
import com.ossanasur.cbconnect.module.comptabilite.entity.EcritureComptable;
import com.ossanasur.cbconnect.module.comptabilite.repository.EcritureComptableRepository;
import com.ossanasur.cbconnect.module.comptabilite.service.ComptabiliteService;
import com.ossanasur.cbconnect.module.finance.dto.request.AnnulerPaiementRequest;
import com.ossanasur.cbconnect.module.finance.dto.request.MarquerPayeRequest;
import com.ossanasur.cbconnect.module.finance.dto.request.PaiementCreateRequest;
import com.ossanasur.cbconnect.module.finance.dto.response.PaiementDetailResponse;
import com.ossanasur.cbconnect.module.finance.dto.response.PaiementResponse;
import com.ossanasur.cbconnect.module.finance.entity.Encaissement;
import com.ossanasur.cbconnect.module.finance.entity.Paiement;
import com.ossanasur.cbconnect.module.finance.mapper.PaiementMapper;
import com.ossanasur.cbconnect.module.finance.repository.EncaissementRepository;
import com.ossanasur.cbconnect.module.finance.repository.PaiementRepository;
import com.ossanasur.cbconnect.module.finance.service.PaiementService;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import com.ossanasur.cbconnect.module.sinistre.entity.Victime;
import com.ossanasur.cbconnect.module.sinistre.repository.SinistreRepository;
import com.ossanasur.cbconnect.module.sinistre.repository.VictimeRepository;
import com.ossanasur.cbconnect.utils.DataResponse;
import com.ossanasur.cbconnect.utils.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaiementServiceImpl implements PaiementService {

    private final PaiementRepository paiementRepository;
    private final SinistreRepository sinistreRepository;
    private final VictimeRepository victimeRepository;
    private final OrganismeRepository organismeRepository;
    private final EncaissementRepository encaissementRepository;
    private final EcritureComptableRepository ecritureRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ComptabiliteService comptabiliteService;
    private final PaiementMapper mapper;

    @Override
    @Transactional
    public DataResponse<PaiementDetailResponse> creer(PaiementCreateRequest request, String loginAuteur) {
        Sinistre sinistre = sinistreRepository.findActiveByTrackingId(request.sinistreTrackingId())
                .orElseThrow(() -> new RessourceNotFoundException("Sinistre introuvable"));

        if (paiementRepository.existsByNumeroChequeEmisAndActiveDataTrueAndDeletedDataFalse(request.numeroChequeEmis())) {
            throw new BadRequestException("Chèque " + request.numeroChequeEmis() + " déjà enregistré");
        }

        Victime victime = request.beneficiaireVictimeTrackingId() == null ? null
                : victimeRepository.findActiveByTrackingId(request.beneficiaireVictimeTrackingId())
                        .orElseThrow(() -> new RessourceNotFoundException("Victime bénéficiaire introuvable"));
        Organisme organisme = request.beneficiaireOrganismeTrackingId() == null ? null
                : organismeRepository.findActiveByTrackingId(request.beneficiaireOrganismeTrackingId())
                        .orElseThrow(() -> new RessourceNotFoundException("Organisme bénéficiaire introuvable"));

        Paiement paiement = mapper.toNewEntity(request, sinistre, victime, organisme, loginAuteur);
        Paiement persisted = paiementRepository.save(paiement);

        var ecritureResp = comptabiliteService.genererEcritureAuto(
                TypeTransactionComptable.PAIEMENT_VICTIME,
                sinistre.getSinistreTrackingId(),
                request.montant(),
                request.numeroChequeEmis(),
                loginAuteur);

        EcritureComptable ecriture = ecritureRepository.findByEcritureTrackingId(ecritureResp.ecritureTrackingId())
                .orElseThrow(() -> new IllegalStateException(
                        "Écriture comptable générée introuvable : " + ecritureResp.ecritureTrackingId()));
        persisted.setEcritureComptable(ecriture);
        persisted = paiementRepository.save(persisted);

        log.info("Paiement {} créé (sinistre={}, montant={}, écriture={})",
                persisted.getPaiementTrackingId(), sinistre.getSinistreTrackingId(),
                request.montant(), ecriture.getNumeroEcriture());

        return DataResponse.created("Règlement enregistré", mapper.toDetailResponse(persisted));
    }

    @Override
    @Transactional
    public DataResponse<PaiementDetailResponse> annuler(UUID paiementTrackingId, AnnulerPaiementRequest request,
            String loginAuteur) {
        Paiement paiement = findActiveOrThrow(paiementTrackingId);
        if (paiement.getStatut() == StatutPaiement.ANNULE) {
            throw new BadRequestException("Paiement déjà annulé");
        }
        if (paiement.getStatut() != StatutPaiement.EMIS && paiement.getStatut() != StatutPaiement.PAYE) {
            throw new BadRequestException("Statut paiement invalide pour annulation : " + paiement.getStatut());
        }

        EcritureComptable ecriture = paiement.getEcritureComptable();
        if (ecriture != null) {
            if (ecriture.getStatut() == StatutEcritureComptable.VALIDEE) {
                comptabiliteService.genererEcritureAuto(
                        TypeTransactionComptable.CONTRA_ECRITURE,
                        paiement.getSinistre().getSinistreTrackingId(),
                        paiement.getMontant(),
                        "Annulation paiement " + paiement.getNumeroChequeEmis(),
                        loginAuteur);
            }
            ecriture.setStatut(StatutEcritureComptable.ANNULEE);
            ecritureRepository.save(ecriture);
        }

        paiement.setStatut(StatutPaiement.ANNULE);
        paiement.setMotifAnnulation(request.motifAnnulation());
        paiement.setAnnulePar(resolveUtilisateur(loginAuteur));
        paiement.setUpdatedBy(loginAuteur);
        Paiement saved = paiementRepository.save(paiement);

        log.info("Paiement {} annulé (motif={})", paiementTrackingId, request.motifAnnulation());
        return DataResponse.success("Règlement annulé", mapper.toDetailResponse(saved));
    }

    @Override
    @Transactional
    public DataResponse<PaiementDetailResponse> marquerPaye(UUID paiementTrackingId, MarquerPayeRequest request,
            String loginAuteur) {
        Paiement paiement = findActiveOrThrow(paiementTrackingId);
        if (paiement.getStatut() != StatutPaiement.EMIS) {
            throw new BadRequestException("Seul un paiement EMIS peut être marqué payé (statut actuel : "
                    + paiement.getStatut() + ")");
        }
        EcritureComptable ecriture = paiement.getEcritureComptable();
        if (ecriture == null || ecriture.getStatut() != StatutEcritureComptable.VALIDEE) {
            throw new BadRequestException("Écriture comptable non validée, marquage paiement impossible");
        }

        paiement.setStatut(StatutPaiement.PAYE);
        paiement.setDatePaiement(request.datePaiement());
        paiement.setUpdatedBy(loginAuteur);
        Paiement saved = paiementRepository.save(paiement);

        log.info("Paiement {} marqué PAYE (date={})", paiementTrackingId, request.datePaiement());
        return DataResponse.success("Paiement confirmé", mapper.toDetailResponse(saved));
    }

    @Override
    @Transactional
    public DataResponse<PaiementDetailResponse> lierEncaissement(UUID paiementTrackingId,
            UUID encaissementTrackingId, String loginAuteur) {
        Paiement paiement = findActiveOrThrow(paiementTrackingId);
        if (paiement.getStatut() != StatutPaiement.PAYE) {
            throw new BadRequestException("Rapprochement encaissement impossible : paiement pas PAYE");
        }
        Encaissement encaissement = encaissementRepository.findActiveByTrackingId(encaissementTrackingId)
                .orElseThrow(() -> new RessourceNotFoundException("Encaissement introuvable"));

        boolean dejaLie = paiement.getEncaissements().stream()
                .anyMatch(e -> e.getEncaissementTrackingId().equals(encaissementTrackingId));
        if (dejaLie) {
            throw new BadRequestException("Encaissement déjà rapproché à ce paiement");
        }
        paiement.getEncaissements().add(encaissement);
        paiement.setUpdatedBy(loginAuteur);
        Paiement saved = paiementRepository.save(paiement);
        return DataResponse.success("Encaissement rapproché", mapper.toDetailResponse(saved));
    }

    @Override
    @Transactional
    public DataResponse<PaiementDetailResponse> delierEncaissement(UUID paiementTrackingId,
            UUID encaissementTrackingId, String loginAuteur) {
        Paiement paiement = findActiveOrThrow(paiementTrackingId);
        boolean removed = paiement.getEncaissements()
                .removeIf(e -> e.getEncaissementTrackingId().equals(encaissementTrackingId));
        if (!removed) {
            throw new RessourceNotFoundException("Encaissement non rattaché à ce paiement");
        }
        paiement.setUpdatedBy(loginAuteur);
        Paiement saved = paiementRepository.save(paiement);
        return DataResponse.success("Encaissement détaché", mapper.toDetailResponse(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<PaiementDetailResponse> getByTrackingId(UUID paiementTrackingId) {
        Paiement paiement = findActiveOrThrow(paiementTrackingId);
        return DataResponse.success(mapper.toDetailResponse(paiement));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<PaiementResponse>> getBySinistre(UUID sinistreTrackingId) {
        List<PaiementResponse> liste = paiementRepository.findBySinistre(sinistreTrackingId).stream()
                .map(mapper::toResponse)
                .toList();
        return DataResponse.success(liste);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<PaiementResponse> rechercher(StatutPaiement statut, LocalDate dateDebut, LocalDate dateFin,
            UUID sinistreTrackingId, int page, int size) {
        var pageResult = paiementRepository.rechercherPaiements(
                statut == null ? null : statut.name(),
                dateDebut, dateFin, sinistreTrackingId,
                PageRequest.of(page, size))
                .map(mapper::toResponse);
        return PaginatedResponse.fromPage(pageResult, "Paiements");
    }

    private Paiement findActiveOrThrow(UUID paiementTrackingId) {
        return paiementRepository.findActiveByTrackingId(paiementTrackingId)
                .orElseThrow(() -> new RessourceNotFoundException("Paiement introuvable"));
    }

    private Utilisateur resolveUtilisateur(String login) {
        return utilisateurRepository.findByEmailOrUsername(login, login).orElse(null);
    }
}
