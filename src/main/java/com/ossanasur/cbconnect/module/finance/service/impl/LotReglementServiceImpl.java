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
    private final com.ossanasur.cbconnect.module.finance.repository.EncaissementRepository encaissementRepository;
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

            String numeroSinistre = s.getNumeroSinistreLocal() != null
                    ? s.getNumeroSinistreLocal()
                    : (s.getNumeroSinistreManuel() != null
                            ? s.getNumeroSinistreManuel()
                            : s.getNumeroSinistreHomologue());

            String numeroPolice = s.getAssure() != null ? s.getAssure().getNumeroPolice() : null;

            String libelle = "";
            if (s.getAssure() != null) {
                String nom = s.getAssure().getNomComplet() != null && !s.getAssure().getNomComplet().isBlank()
                        ? s.getAssure().getNomComplet()
                        : (((s.getAssure().getNomAssure() != null ? s.getAssure().getNomAssure() : "") + " "
                            + (s.getAssure().getPrenomAssure() != null ? s.getAssure().getPrenomAssure() : "")).trim());
                String immat = s.getAssure().getImmatriculation();
                if (!nom.isBlank() && immat != null && !immat.isBlank()) {
                    libelle = nom + " — " + immat;
                } else if (!nom.isBlank()) {
                    libelle = nom;
                } else if (immat != null) {
                    libelle = immat;
                }
            }
            if (libelle.isBlank()) libelle = s.getLibelle();

            java.time.LocalDate dateSinistre = s.getDateAccident() != null
                    ? s.getDateAccident()
                    : s.getDateDeclaration();

            // Type d'expertise + date du rapport (depuis l'affectation et l'expertise correspondantes)
            String typeExpertise = sinistreRepository.findTypeExpertiseForExpert(
                    s.getHistoriqueId(), expert.getHistoriqueId());
            java.time.LocalDate dateRapport = sinistreRepository.findDateRapportForExpert(
                    s.getHistoriqueId(), expert.getHistoriqueId());

            BigDecimal totalEnc = encaissementRepository.sumMontantActifBySinistre(s.getSinistreTrackingId());
            BigDecimal totalPay = paiementRepository.sumPaiementsActifsBySinistre(s.getSinistreTrackingId());
            if (totalEnc == null) totalEnc = BigDecimal.ZERO;
            if (totalPay == null) totalPay = BigDecimal.ZERO;
            BigDecimal fondsDispo = totalEnc.subtract(totalPay);

            return new SinistrePayableResponse(
                    s.getSinistreTrackingId(),
                    libelle,
                    numeroSinistre,
                    numeroPolice,
                    dateSinistre,
                    typeExpertise,
                    dateRapport,
                    expert.getMontExpertise(),
                    fondsDispo,
                    totalEnc,
                    totalPay,
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

        // 2. Calculs et création du lot
        BigDecimal tauxTva = expert.getPays() != null && expert.getPays().getTauxTva() != null
                ? expert.getPays().getTauxTva()
                : new BigDecimal("0.18");

        // 1. Pré-validation : RÈGLE A + pas de doublon HONORAIRES + fonds suffisants
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

            // Contrôle solde : fonds_dispo = encaissements ENCAISSE - paiements actifs
            BigDecimal ht = ligne.montantHt();
            BigDecimal tva = ht.multiply(tauxTva).setScale(2, RoundingMode.HALF_UP);
            BigDecimal ttcLigne = ht.add(tva);

            BigDecimal totalEncaisse = encaissementRepository.sumMontantActifBySinistre(
                    ligne.sinistreTrackingId());
            BigDecimal totalDejaPaye = paiementRepository.sumPaiementsActifsBySinistre(
                    ligne.sinistreTrackingId());
            if (totalEncaisse == null) totalEncaisse = BigDecimal.ZERO;
            if (totalDejaPaye == null) totalDejaPaye = BigDecimal.ZERO;
            BigDecimal fondsDispo = totalEncaisse.subtract(totalDejaPaye);

            if (fondsDispo.compareTo(ttcLigne) < 0) {
                String numSin = sinistre.getNumeroSinistreLocal() != null
                        ? sinistre.getNumeroSinistreLocal()
                        : (sinistre.getNumeroSinistreManuel() != null
                                ? sinistre.getNumeroSinistreManuel()
                                : sinistre.getNumeroSinistreHomologue());
                if (numSin == null || numSin.isBlank()) numSin = "#" + sinistre.getHistoriqueId();

                BigDecimal manque = ttcLigne.subtract(fondsDispo);
                throw new BadRequestException(String.format(
                        "Fonds insuffisants sur le sinistre %s. Solde disponible : %s FCFA " +
                                "(encaissé : %s FCFA, déjà réglé : %s FCFA). Règlement demandé : %s FCFA. " +
                                "Manque : %s FCFA.",
                        numSin, fondsDispo, totalEncaisse, totalDejaPaye, ttcLigne, manque));
            }
        }

        String motifLibelle = paramMotifService.resolveLibelleByLibelleAndType(
                libelleMotifPourTypeExpert(expert.getTypeExpert()),
                TypeMotif.REGLEMENT);

        // Numéro lot : LT{seq:03d}/{yyyy} avec séquence annuelle globale
        int annee = LocalDate.now().getYear();
        long countAnnee = lotRepository.countByYear(annee);
        String numeroLot = String.format("LT%03d/%d", countAnnee + 1, annee);

        LotReglement lot = LotReglement.builder()
                .lotTrackingId(UUID.randomUUID())
                .numeroLot(numeroLot)
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
        lot = lotRepository.saveAndFlush(lot);

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
        Integer expertId = expertTrackingId == null ? null
                : expertRepository.findActiveByTrackingId(expertTrackingId)
                        .map(Expert::getHistoriqueId).orElse(null);
        String statutStr = statut == null ? null : statut.name();

        var pageData = lotRepository.findActiveFiltered(expertId, statutStr, PageRequest.of(page, size))
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
