package com.ossanasur.cbconnect.module.finance.service.impl;

import com.ossanasur.cbconnect.common.enums.*;
import com.ossanasur.cbconnect.exception.BadRequestException;
import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.module.expertise.entity.Expert;
import com.ossanasur.cbconnect.module.expertise.repository.ExpertRepository;
import com.ossanasur.cbconnect.module.finance.dto.request.CreerLotRequest;
import com.ossanasur.cbconnect.module.finance.dto.request.SaisieComptableLotRequest;
import com.ossanasur.cbconnect.module.finance.dto.response.LotReglementResponse;
import com.ossanasur.cbconnect.module.finance.dto.response.SinistrePayableResponse;
import com.ossanasur.cbconnect.module.finance.entity.LotReglement;
import com.ossanasur.cbconnect.module.finance.entity.Paiement;
import com.ossanasur.cbconnect.module.finance.mapper.LotReglementMapper;
import com.ossanasur.cbconnect.module.finance.repository.LotReglementRepository;
import com.ossanasur.cbconnect.module.finance.repository.PaiementRepository;
import com.ossanasur.cbconnect.module.finance.service.EncaissementGuardService;
import com.ossanasur.cbconnect.module.finance.service.LotReglementService;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import com.ossanasur.cbconnect.module.sinistre.repository.SinistreRepository;
import com.ossanasur.cbconnect.utils.DataResponse;
import com.ossanasur.cbconnect.utils.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LotReglementServiceImpl implements LotReglementService {

    private final LotReglementRepository lotRepository;
    private final ExpertRepository expertRepository;
    private final SinistreRepository sinistreRepository;
    private final PaiementRepository paiementRepository;
    private final LotReglementMapper lotMapper;
    private final EncaissementGuardService guardService;
    private final ParamMotifServiceImpl paramMotifService;
    private final PaiementBeneficiaireValidator beneficiaireValidator;
    private final PaiementServiceImpl paiementService;

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<SinistrePayableResponse>> listerSinistresPayables(UUID expertTrackingId) {
        Expert expert = expertRepository.findActiveByTrackingId(expertTrackingId)
                .orElseThrow(() -> new RessourceNotFoundException("Expert introuvable"));

        List<Sinistre> sinistres = sinistreRepository.findPayablesForExpert(expert.getHistoriqueId());

        List<SinistrePayableResponse> list = sinistres.stream().map(s -> {
            boolean dejaPaye = paiementRepository.existsHonorairesActifByExpertAndSinistre(
                    expertTrackingId, s.getSinistreTrackingId());
            return new SinistrePayableResponse(
                    s.getSinistreTrackingId(),
                    s.getLibelle(),
                    null,   // numeroSinistre — getter absent sur Sinistre
                    null,   // numeroPolice
                    null,   // dateSinistre
                    null,   // typeExpertise
                    null,   // dateRapport
                    expert.getMontExpertise(),
                    dejaPaye
            );
        }).toList();

        return DataResponse.success(null, list);
    }

    @Override
    @Transactional
    public DataResponse<LotReglementResponse> creerLot(CreerLotRequest req, String loginAuteur) {
        Expert expert = expertRepository.findActiveByTrackingId(req.expertTrackingId())
                .orElseThrow(() -> new RessourceNotFoundException("Expert introuvable"));

        if (req.lignes() == null || req.lignes().isEmpty()) {
            throw new BadRequestException("Au moins un sinistre doit être sélectionné");
        }

        // 1. Pré-validation
        for (var ligne : req.lignes()) {
            Sinistre sinistre = sinistreRepository.findActiveByTrackingId(ligne.sinistreTrackingId())
                    .orElseThrow(() -> new RessourceNotFoundException(
                            "Sinistre introuvable : " + ligne.sinistreTrackingId()));

            guardService.verifierRegleA(ligne.sinistreTrackingId());

            if (paiementRepository.existsHonorairesActifByExpertAndSinistre(
                    req.expertTrackingId(), ligne.sinistreTrackingId())) {
                throw new BadRequestException(
                        "Un règlement HONORAIRES actif existe déjà sur le sinistre " + sinistre.getLibelle());
            }
        }

        // 2. Calculs et création du lot
        BigDecimal tauxTva = expert.getPays() != null && expert.getPays().getTauxTva() != null
                ? expert.getPays().getTauxTva()
                : new BigDecimal("0.18");

        String motifLibelle = paramMotifService.resolveLibelleByLibelleAndType(
                libelleMotifPourTypeExpert(expert.getTypeExpert()),
                TypeMotif.REGLEMENT);

        LotReglement lot = LotReglement.builder()
                .lotTrackingId(UUID.randomUUID())
                .expert(expert)
                .tauxRetenue(req.tauxRetenue())
                .statut(StatutLotReglement.EMIS)
                .nombreReglements(0)
                .montantTtcTotal(BigDecimal.ZERO)
                .montantTvaTotal(BigDecimal.ZERO)
                .montantTaxeTotal(BigDecimal.ZERO)
                .activeData(true)
                .deletedData(false)
                .fromTable(TypeTable.LOT_REGLEMENT)
                .createdBy(loginAuteur)
                .createdAt(LocalDateTime.now())
                .build();
        lot = lotRepository.save(lot);

        // 3. Création des N paiements
        BigDecimal totalTtc = BigDecimal.ZERO;
        BigDecimal totalTva = BigDecimal.ZERO;
        BigDecimal totalTaxe = BigDecimal.ZERO;

        List<Paiement> paiements = new ArrayList<>();
        for (var ligne : req.lignes()) {
            Sinistre sinistre = sinistreRepository.findActiveByTrackingId(ligne.sinistreTrackingId())
                    .orElseThrow();

            BigDecimal ht = ligne.montantHt();
            BigDecimal tva = ht.multiply(tauxTva).setScale(2, RoundingMode.HALF_UP);
            BigDecimal ttc = ht.add(tva);
            BigDecimal taxe = ttc.multiply(req.tauxRetenue().getValeur()).setScale(2, RoundingMode.HALF_UP);

            beneficiaireValidator.valider(CategorieReglement.HONORAIRES, sinistre, null, null, expert);

            Paiement p = Paiement.builder()
                    .paiementTrackingId(UUID.randomUUID())
                    .sinistre(sinistre)
                    .beneficiaire(expert.getNomComplet())
                    .beneficiaireExpert(expert)
                    .montant(ttc)
                    .montantTtc(ttc)
                    .montantTva(tva)
                    .montantTaxe(taxe)
                    .categorie(CategorieReglement.HONORAIRES)
                    .motif(motifLibelle)
                    .lotReglement(lot)
                    .statut(StatutPaiement.EMIS)
                    .typePrejudice(TypePrejudice.MATERIEL)
                    .dateEmission(LocalDate.now())
                    .activeData(true)
                    .deletedData(false)
                    .fromTable(TypeTable.PAIEMENT)
                    .createdBy(loginAuteur)
                    .createdAt(LocalDateTime.now())
                    .build();

            Paiement saved = paiementService.persisterPaiementAvecNumero(p, TypeOperationFinanciere.REGLEMENT_TECHNIQUE);
            paiements.add(saved);

            totalTtc = totalTtc.add(ttc);
            totalTva = totalTva.add(tva);
            totalTaxe = totalTaxe.add(taxe);
        }

        lot.setNombreReglements(paiements.size());
        lot.setMontantTtcTotal(totalTtc);
        lot.setMontantTvaTotal(totalTva);
        lot.setMontantTaxeTotal(totalTaxe);
        lotRepository.save(lot);

        log.info("Lot règlement créé {} (expert={}, nb={}, ttc={}, taxe={})",
                lot.getLotTrackingId(), expert.getExpertTrackingId(),
                paiements.size(), totalTtc, totalTaxe);

        return DataResponse.created("Lot de règlements créé", lotMapper.toResponse(lot, paiements));
    }

    @Override
    @Transactional
    public DataResponse<LotReglementResponse> validerTechniqueLot(UUID lotTrackingId, String loginAuteur) {
        LotReglement lot = findActiveOrThrow(lotTrackingId);

        if (lot.getStatut() != StatutLotReglement.EMIS) {
            throw new BadRequestException(
                    "Le lot doit être EMIS (actuel : " + lot.getStatut() + ")");
        }

        List<Paiement> paiements = paiementRepository.findByLotReglement(lot);

        for (Paiement p : paiements) {
            if (p.getStatut() != StatutPaiement.EMIS) {
                throw new BadRequestException(
                        "Le paiement " + p.getNumeroPaiement() + " n'est pas en statut EMIS");
            }
            p.setStatut(StatutPaiement.REGLEMENT_TECHNIQUE_VALIDE);
            p.setUpdatedBy(loginAuteur);
            paiementRepository.save(p);
        }

        lot.setStatut(StatutLotReglement.REGLEMENT_TECHNIQUE_VALIDE);
        lot.setUpdatedBy(loginAuteur);
        lotRepository.save(lot);

        return DataResponse.success("Lot validé techniquement", lotMapper.toResponse(lot, paiements));
    }

    @Override
    @Transactional
    public DataResponse<LotReglementResponse> saisirComptableLot(
            UUID lotTrackingId, SaisieComptableLotRequest req, String loginAuteur) {

        LotReglement lot = findActiveOrThrow(lotTrackingId);

        if (lot.getStatut() != StatutLotReglement.REGLEMENT_TECHNIQUE_VALIDE) {
            throw new BadRequestException(
                    "Le lot doit être REGLEMENT_TECHNIQUE_VALIDE (actuel : " + lot.getStatut() + ")");
        }

        if (paiementRepository.existsByNumeroChequeEmisAndActiveDataTrueAndDeletedDataFalse(req.numeroChequeEmis())) {
            throw new BadRequestException("Le chèque n° " + req.numeroChequeEmis() + " est déjà utilisé");
        }

        List<Paiement> rtList = paiementRepository.findByLotReglement(lot);
        List<Paiement> rcList = new ArrayList<>();

        for (Paiement rt : rtList) {
            if (rt.getStatut() != StatutPaiement.REGLEMENT_TECHNIQUE_VALIDE) {
                throw new BadRequestException(
                        "Le RT " + rt.getNumeroPaiement() + " n'est pas validé techniquement");
            }

            Paiement rc = Paiement.builder()
                    .paiementTrackingId(UUID.randomUUID())
                    .sinistre(rt.getSinistre())
                    .beneficiaire(rt.getBeneficiaire())
                    .beneficiaireExpert(rt.getBeneficiaireExpert())
                    .beneficiaireVictime(rt.getBeneficiaireVictime())
                    .beneficiaireOrganisme(rt.getBeneficiaireOrganisme())
                    .montant(rt.getMontant())
                    .montantTtc(rt.getMontantTtc())
                    .montantTva(rt.getMontantTva())
                    .montantTaxe(rt.getMontantTaxe())
                    .categorie(rt.getCategorie())
                    .motif(rt.getMotif())
                    .lotReglement(lot)
                    .typePrejudice(rt.getTypePrejudice())
                    .dateEmission(req.dateEmissionCheque())
                    .dateEmissionCheque(req.dateEmissionCheque())
                    .numeroChequeEmis(req.numeroChequeEmis())
                    .banqueCheque(req.banqueCheque())
                    .modePaiement("CHEQUE")
                    .statut(StatutPaiement.REGLEMENT_COMPTABLE_VALIDE)
                    .parentCodeId(rt.getPaiementTrackingId().toString())
                    .activeData(true)
                    .deletedData(false)
                    .fromTable(TypeTable.PAIEMENT)
                    .createdBy(loginAuteur)
                    .createdAt(LocalDateTime.now())
                    .build();

            Paiement saved = paiementService.persisterPaiementAvecNumero(rc, TypeOperationFinanciere.REGLEMENT_COMPTABLE);
            rcList.add(saved);
        }

        lot.setNumeroChequeGlobal(req.numeroChequeEmis());
        lot.setBanqueCheque(req.banqueCheque());
        lot.setDateEmissionCheque(req.dateEmissionCheque());
        lot.setStatut(StatutLotReglement.REGLEMENT_COMPTABLE_VALIDE);
        lot.setUpdatedBy(loginAuteur);
        lotRepository.save(lot);

        return DataResponse.success("Lot comptable saisi", lotMapper.toResponse(lot, rcList));
    }

    @Override
    @Transactional
    public DataResponse<LotReglementResponse> validerComptableLot(UUID lotTrackingId, String loginAuteur) {
        LotReglement lot = findActiveOrThrow(lotTrackingId);

        if (lot.getStatut() != StatutLotReglement.REGLEMENT_COMPTABLE_VALIDE) {
            throw new BadRequestException(
                    "Le lot doit être REGLEMENT_COMPTABLE_VALIDE (actuel : " + lot.getStatut() + ")");
        }

        List<Paiement> rcList = paiementRepository.findByLotReglementAndStatut(
                lot, StatutPaiement.REGLEMENT_COMPTABLE_VALIDE);

        for (Paiement rc : rcList) {
            paiementService.validerComptable(rc.getPaiementTrackingId(), loginAuteur);
        }

        lot.setStatut(StatutLotReglement.PAYE);
        lot.setUpdatedBy(loginAuteur);
        lotRepository.save(lot);

        // Recharger les paiements après validerComptable (qui a créé des nouvelles lignes en statut PAYE)
        List<Paiement> paiementsApres = paiementRepository.findByLotReglement(lot);
        return DataResponse.success("Lot validé comptable", lotMapper.toResponse(lot, paiementsApres));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<LotReglementResponse> getByTrackingId(UUID lotTrackingId) {
        LotReglement lot = findActiveOrThrow(lotTrackingId);
        List<Paiement> paiements = paiementRepository.findByLotReglement(lot);
        return DataResponse.success(null, lotMapper.toResponse(lot, paiements));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<LotReglementResponse> lister(
            UUID expertTrackingId, StatutLotReglement statut, int page, int size) {
        Expert expert = expertTrackingId == null ? null
                : expertRepository.findActiveByTrackingId(expertTrackingId).orElse(null);

        var pageData = lotRepository.findActiveFiltered(expert, statut, PageRequest.of(page, size))
                .map(lot -> lotMapper.toResponse(lot, paiementRepository.findByLotReglement(lot)));

        return PaginatedResponse.fromPage(pageData, "Lots de règlements");
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private LotReglement findActiveOrThrow(UUID lotTrackingId) {
        return lotRepository.findActiveByTrackingId(lotTrackingId)
                .orElseThrow(() -> new RessourceNotFoundException(
                        "Lot de règlement introuvable : " + lotTrackingId));
    }

    private String libelleMotifPourTypeExpert(TypeExpert typeExpert) {
        return switch (typeExpert) {
            case MEDICAL -> "Honoraires expert médical";
            case AUTOMOBILE -> "Honoraires expert matériel";
        };
    }
}
