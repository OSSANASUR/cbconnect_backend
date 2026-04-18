package com.ossanasur.cbconnect.module.comptabilite.service.impl;

import com.ossanasur.cbconnect.common.enums.*;
import com.ossanasur.cbconnect.exception.*;
import com.ossanasur.cbconnect.module.auth.repository.UtilisateurRepository;
import com.ossanasur.cbconnect.module.comptabilite.dto.response.EcritureResponse;
import com.ossanasur.cbconnect.module.comptabilite.entity.*;
import com.ossanasur.cbconnect.module.comptabilite.mapper.ComptabiliteMapper;
import com.ossanasur.cbconnect.module.comptabilite.repository.*;
import com.ossanasur.cbconnect.module.comptabilite.service.ComptabiliteService;
import com.ossanasur.cbconnect.module.sinistre.repository.SinistreRepository;
import com.ossanasur.cbconnect.utils.DataResponse;
import com.ossanasur.cbconnect.utils.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComptabiliteServiceImpl implements ComptabiliteService {
    private final EcritureComptableRepository ecritureRepository;
    private final RegleEcritureRepository regleRepository;
    @SuppressWarnings("unused")
    private final PlanComptableRepository planRepository;
    private final SinistreRepository sinistreRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ComptabiliteMapper mapper;

    @Override
    @Transactional
    public EcritureResponse genererEcritureAuto(TypeTransactionComptable type, UUID sinistreId, BigDecimal montant,
            String ref, String loginAuteur) {
        var sinistre = sinistreRepository.findActiveByTrackingId(sinistreId).orElse(null);
        var regle = regleRepository.findByTypeTransactionAndActifTrue(type)
                .orElseThrow(() -> new BadRequestException("Aucune règle comptable pour " + type));
        int annee = LocalDate.now().getYear();
        long seq = ecritureRepository.findMaxSeq(annee) + 1;
        String numero = "ECR-" + annee + "-" + String.format("%06d", seq);
        var auteur = utilisateurRepository.findByEmailAndActiveDataTrueAndDeletedDataFalse(loginAuteur).orElse(null);
        EcritureComptable ecriture = EcritureComptable.builder()
                .ecritureTrackingId(UUID.randomUUID()).numeroEcriture(numero)
                .typeTransaction(type).dateEcriture(LocalDate.now())
                .libelle(regle.getLibelle() + " - " + ref)
                .montantTotal(montant).statut(StatutEcritureComptable.BROUILLON)
                .journal(regle.getJournal()).sinistre(sinistre).saisiPar(auteur)
                .build();
        List<LigneEcriture> lignes = new ArrayList<>();
        if (regle.getLignes() != null) {
            for (var ligneRegle : regle.getLignes()) {
                BigDecimal montantLigne = resoudreMontant(ligneRegle.getExpressionMontant(), montant);
                LigneEcriture ligne = LigneEcriture.builder()
                        .ecriture(ecriture).compte(ligneRegle.getCompte())
                        .sens(ligneRegle.getSensLigne()).montant(montantLigne)
                        .libelleLigne(ligneRegle.getLibelleLigne()).ordre(ligneRegle.getOrdre()).build();
                lignes.add(ligne);
            }
        }
        ecriture.setLignes(lignes);
        EcritureComptable saved = ecritureRepository.save(ecriture);
        log.info("Écriture comptable générée : {} - {} FCFA", numero, montant);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public DataResponse<EcritureResponse> validerEcriture(UUID id, String loginAuteur) {
        var ecriture = ecritureRepository.findByEcritureTrackingId(id)
                .orElseThrow(() -> new RessourceNotFoundException("Écriture introuvable"));
        var validateur = utilisateurRepository.findByEmailAndActiveDataTrueAndDeletedDataFalse(loginAuteur)
                .orElse(null);
        ecriture.setStatut(StatutEcritureComptable.VALIDEE);
        ecriture.setDateValidation(LocalDateTime.now());
        ecriture.setValidePar(validateur);
        return DataResponse.success("Écriture validée", mapper.toResponse(ecritureRepository.save(ecriture)));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<EcritureResponse>> getBySinistre(UUID sinistreId) {
        return DataResponse.success(ecritureRepository.findBySinistre(sinistreId)
                .stream().map(mapper::toResponse).collect(Collectors.toList()));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<EcritureResponse> getByPeriode(LocalDate debut, LocalDate fin, int page, int size) {
        return PaginatedResponse.fromPage(ecritureRepository.findByPeriode(debut, fin, PageRequest.of(page, size))
                .map(mapper::toResponse), "Écritures comptables");
    }

    private BigDecimal resoudreMontant(String expression, BigDecimal montantBase) {
        return switch (expression) {
            case "MONTANT_CHEQUE", "MONTANT_BASE" -> montantBase;
            case "FRAIS_GESTION" ->
                montantBase.multiply(new BigDecimal("0.05")).setScale(2, java.math.RoundingMode.HALF_UP);
            default -> montantBase;
        };
    }
}
