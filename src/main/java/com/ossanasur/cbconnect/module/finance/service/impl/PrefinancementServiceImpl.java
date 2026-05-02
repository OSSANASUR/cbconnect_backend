package com.ossanasur.cbconnect.module.finance.service.impl;

import com.ossanasur.cbconnect.common.enums.StatutPrefinancement;
import com.ossanasur.cbconnect.common.enums.TypeOperationFinanciere;
import com.ossanasur.cbconnect.common.enums.TypeTable;
import com.ossanasur.cbconnect.exception.BadRequestException;
import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.module.auth.repository.UtilisateurRepository;
import com.ossanasur.cbconnect.module.finance.dto.request.AnnulerPrefinancementRequest;
import com.ossanasur.cbconnect.module.finance.dto.request.PrefinancementCreateRequest;
import com.ossanasur.cbconnect.module.finance.dto.request.RembourserPrefinancementRequest;
import com.ossanasur.cbconnect.module.finance.dto.response.CouvertureFinanciereResponse;
import com.ossanasur.cbconnect.module.finance.dto.response.CouvertureSinistreResponse;
import com.ossanasur.cbconnect.module.finance.dto.response.PrefinancementDetailResponse;
import com.ossanasur.cbconnect.module.finance.dto.response.PrefinancementResponse;
import com.ossanasur.cbconnect.module.finance.dto.response.RemboursementSuggestionResponse;
import com.ossanasur.cbconnect.module.finance.dto.response.RemboursementSuggestionResponse.EncaissementCandidat;
import com.ossanasur.cbconnect.module.finance.service.PaiementImputationService;
import com.ossanasur.cbconnect.module.finance.entity.Encaissement;
import com.ossanasur.cbconnect.module.finance.entity.Prefinancement;
import com.ossanasur.cbconnect.module.finance.entity.PrefinancementRemboursement;
import com.ossanasur.cbconnect.module.finance.mapper.PrefinancementMapper;
import com.ossanasur.cbconnect.module.finance.repository.EncaissementRepository;
import com.ossanasur.cbconnect.module.finance.repository.PaiementRepository;
import com.ossanasur.cbconnect.module.finance.repository.PrefinancementRemboursementRepository;
import com.ossanasur.cbconnect.module.finance.repository.PrefinancementRepository;
import com.ossanasur.cbconnect.module.finance.service.NumeroOperationGenerator;
import com.ossanasur.cbconnect.module.finance.service.PrefinancementService;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import com.ossanasur.cbconnect.module.sinistre.repository.SinistreRepository;
import com.ossanasur.cbconnect.utils.DataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrefinancementServiceImpl implements PrefinancementService {

        private final PrefinancementRepository prefinancementRepository;
        private final PrefinancementRemboursementRepository remboursementRepository;
        private final SinistreRepository sinistreRepository;
        private final EncaissementRepository encaissementRepository;
        private final UtilisateurRepository utilisateurRepository;
        private final NumeroOperationGenerator numeroOperationGenerator;
        private final PrefinancementMapper mapper;
        private final PaiementRepository paiementRepository;
        private final PaiementImputationService paiementImputationService;

        /**
         * Crée un préfinancement en statut DEMANDE.
         * Numéro PF non encore généré (alloué à la validation).
         */
        @Override
        @Transactional
        public DataResponse<PrefinancementDetailResponse> creer(
                        PrefinancementCreateRequest request, String loginAuteur) {

                Sinistre sinistre = sinistreRepository.findActiveByTrackingId(request.sinistreTrackingId())
                                .orElseThrow(() -> new RessourceNotFoundException("Sinistre introuvable"));

                Prefinancement p = Prefinancement.builder()
                                .prefinancementTrackingId(UUID.randomUUID())
                                .sinistre(sinistre)
                                .montantPrefinance(request.montantPrefinance())
                                .datePrefinancement(request.datePrefinancement())
                                .motifDemande(request.motifDemande())
                                .statut(StatutPrefinancement.DEMANDE)
                                .estRembourse(false)
                                .montantRembourse(BigDecimal.ZERO)
                                .createdBy(loginAuteur)
                                .activeData(true)
                                .deletedData(false)
                                .fromTable(TypeTable.PREFINANCEMENT)
                                .build();

                Prefinancement saved = prefinancementRepository.save(p);
                log.info("Préfinancement DEMANDE créé {} (sinistre={}, montant={})",
                                saved.getPrefinancementTrackingId(),
                                sinistre.getSinistreTrackingId(),
                                request.montantPrefinance());

                return DataResponse.created("Demande de préfinancement enregistrée",
                                mapper.toDetailResponse(saved));
        }

        @Override
        @Transactional(readOnly = true)
        public DataResponse<PrefinancementDetailResponse> getByTrackingId(UUID id) {
                return DataResponse.success(mapper.toDetailResponse(findActiveOrThrow(id)));
        }

        @Override
        @Transactional(readOnly = true)
        public DataResponse<List<PrefinancementResponse>> getBySinistre(UUID sinistreTrackingId) {
                List<PrefinancementResponse> liste = prefinancementRepository
                                .findActiveBySinistre(sinistreTrackingId)
                                .stream().map(mapper::toResponse).toList();
                return DataResponse.success(liste);
        }

        private Prefinancement findActiveOrThrow(UUID id) {
                return prefinancementRepository.findActiveByTrackingId(id)
                                .orElseThrow(() -> new RessourceNotFoundException("Préfinancement introuvable"));
        }

        private Utilisateur resolveUtilisateur(String login) {
                return utilisateurRepository.findByEmailOrUsername(login, login).orElse(null);
        }

        /**
         * Valide une demande (COMPTABLE) :
         * - statut DEMANDE → VALIDE
         * - Génère le numéro PF (NumeroOperationGenerator) avec retry sur collision
         * - Génère l'écriture comptable PREFINANCEMENT
         * - Set dateValidation, validePar, FK ecritureComptable
         */
        @Override
        @Transactional
        public DataResponse<PrefinancementDetailResponse> valider(UUID id, String loginAuteur) {
                Prefinancement p = findActiveOrThrow(id);

                if (p.getStatut() != StatutPrefinancement.DEMANDE) {
                        throw new BadRequestException(
                                        "Seul un préfinancement en DEMANDE peut être validé (statut actuel : "
                                                        + p.getStatut() + ")");
                }

                // Numéro PF + retry collision
                final int maxRetries = 5;
                DataIntegrityViolationException last = null;
                for (int attempt = 1; attempt <= maxRetries; attempt++) {
                        try {
                                String numero = numeroOperationGenerator.genererNumero(
                                                TypeOperationFinanciere.PREFINANCEMENT, p.getSinistre());
                                p.setNumeroPrefinancement(numero);
                                prefinancementRepository.saveAndFlush(p);
                                break;
                        } catch (DataIntegrityViolationException e) {
                                last = e;
                                log.warn("Collision numero_operation PF (tentative {}/{}) sinistre={}",
                                                attempt, maxRetries, p.getSinistre().getHistoriqueId());
                                if (attempt == maxRetries)
                                        throw last;
                        }
                }

                // Écriture comptable PREFINANCEMENT — désactivée temporairement
                // var ecritureResp = comptabiliteService.genererEcritureAuto(
                // TypeTransactionComptable.PREFINANCEMENT,
                // p.getSinistre().getSinistreTrackingId(),
                // p.getMontantPrefinance(),
                // "Préfinancement " + p.getNumeroPrefinancement(),
                // loginAuteur);

                // EcritureComptable ecriture = ecritureRepository
                // .findByEcritureTrackingId(ecritureResp.ecritureTrackingId())
                // .orElseThrow(() -> new IllegalStateException(
                // "Écriture comptable générée introuvable : " +
                // ecritureResp.ecritureTrackingId()));

                p.setStatut(StatutPrefinancement.VALIDE);
                p.setDateValidation(LocalDateTime.now());
                p.setValidePar(resolveUtilisateur(loginAuteur));
                // p.setEcritureComptable(ecriture);
                p.setUpdatedBy(loginAuteur);

                Prefinancement saved = prefinancementRepository.save(p);
                log.info("Préfinancement validé {} → VALIDE (numéro={})",
                                id, saved.getNumeroPrefinancement());

                // log.info("Préfinancement validé {} → VALIDE (numéro={}, écriture={})",
                // id, saved.getNumeroPrefinancement(), ecriture.getNumeroEcriture());

                return DataResponse.success("Préfinancement validé", mapper.toDetailResponse(saved));
        }

        /**
         * Annule un préfinancement.
         * - Depuis DEMANDE : transition simple, pas d'écriture
         * - Depuis VALIDE : génère CONTRA_ECRITURE pour annuler la PREFINANCEMENT
         * - Depuis REMBOURSE_PARTIEL/TOTAL : refusé (préserve la cohérence comptable)
         */
        @Override
        @Transactional
        public DataResponse<PrefinancementDetailResponse> annuler(
                        UUID id, AnnulerPrefinancementRequest request, String loginAuteur) {
                Prefinancement p = findActiveOrThrow(id);

                if (p.getStatut() == StatutPrefinancement.ANNULE) {
                        throw new BadRequestException("Ce préfinancement est déjà annulé");
                }
                if (p.getStatut() == StatutPrefinancement.REMBOURSE_PARTIEL
                                || p.getStatut() == StatutPrefinancement.REMBOURSE_TOTAL) {
                        throw new BadRequestException(
                                        "Annulation impossible : remboursement déjà effectué (statut "
                                                        + p.getStatut() + ").");
                }

                // Si VALIDE → contre-écriture
                if (p.getStatut() == StatutPrefinancement.VALIDE) {
                        // EcritureComptable ecriture = p.getEcritureComptable();
                        // if (ecriture != null && ecriture.getStatut() ==
                        // StatutEcritureComptable.VALIDEE) {
                        // comptabiliteService.genererEcritureAuto(
                        // TypeTransactionComptable.CONTRA_ECRITURE,
                        // p.getSinistre().getSinistreTrackingId(),
                        // p.getMontantPrefinance(),
                        // "Annulation préfinancement " + p.getNumeroPrefinancement(),
                        // loginAuteur);
                        // ecriture.setStatut(StatutEcritureComptable.ANNULEE);
                        // ecritureRepository.save(ecriture);
                        // }
                }

                p.setStatut(StatutPrefinancement.ANNULE);
                p.setMotifAnnulation(request.motifAnnulation());
                p.setAnnulePar(resolveUtilisateur(loginAuteur));
                p.setUpdatedBy(loginAuteur);

                Prefinancement saved = prefinancementRepository.save(p);
                log.info("Préfinancement {} annulé par {} (motif={})",
                                id, loginAuteur, request.motifAnnulation());

                return DataResponse.success("Préfinancement annulé", mapper.toDetailResponse(saved));
        }

        /**
         * Calcule le montant suggéré (= reste à rembourser) et liste les encaissements
         * candidats du sinistre avec leur solde disponible (= montantCheque -
         * imputations existantes).
         */
        @Override
        @Transactional(readOnly = true)
        public DataResponse<RemboursementSuggestionResponse> getRemboursementSuggere(UUID id) {
                Prefinancement p = findActiveOrThrow(id);
                if (p.getStatut() != StatutPrefinancement.VALIDE
                                && p.getStatut() != StatutPrefinancement.REMBOURSE_PARTIEL) {
                        throw new BadRequestException(
                                        "Suggestion de remboursement réservée aux préfinancements "
                                                        + "VALIDE ou REMBOURSE_PARTIEL (statut actuel : "
                                                        + p.getStatut() + ")");
                }
                BigDecimal rembourse = remboursementRepository.sumMontantByPrefinancement(p.getHistoriqueId());
                BigDecimal reste = p.getMontantPrefinance().subtract(rembourse);

                List<Encaissement> encaissementsActifs = encaissementRepository
                                .findActifsBySinistre(p.getSinistre().getSinistreTrackingId());
                List<EncaissementCandidat> candidats = encaissementsActifs.stream().map(e -> {
                        BigDecimal dejaImpute = remboursementRepository.sumMontantByEncaissement(e.getHistoriqueId());
                        BigDecimal solde = e.getMontantCheque().subtract(dejaImpute);
                        return new EncaissementCandidat(
                                        e.getEncaissementTrackingId(),
                                        e.getNumeroCheque(),
                                        e.getMontantCheque(),
                                        solde,
                                        e.getDateEncaissement(),
                                        e.getStatutCheque());
                }).filter(c -> c.soldeDispoApresImputations().compareTo(BigDecimal.ZERO) > 0)
                                .toList();

                BigDecimal soldeMaxDispo = candidats.stream()
                                .map(EncaissementCandidat::soldeDispoApresImputations)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal montantSuggere = reste.min(soldeMaxDispo);

                return DataResponse.success(new RemboursementSuggestionResponse(reste, montantSuggere, candidats));
        }

        /**
         * Crée un PrefinancementRemboursement (imputation d'un encaissement source
         * sur le préfinancement) et génère l'écriture REMB_PREFINANCEMENT.
         * Met à jour le statut : REMBOURSE_PARTIEL ou REMBOURSE_TOTAL.
         */
        @Override
        @Transactional
        public DataResponse<PrefinancementDetailResponse> rembourser(
                        UUID id, RembourserPrefinancementRequest request, String loginAuteur) {
                Prefinancement p = findActiveOrThrow(id);

                if (p.getStatut() != StatutPrefinancement.VALIDE
                                && p.getStatut() != StatutPrefinancement.REMBOURSE_PARTIEL) {
                        throw new BadRequestException(
                                        "Remboursement impossible : préfinancement non validé ou déjà soldé "
                                                        + "(statut actuel : " + p.getStatut() + ")");
                }

                Encaissement enc = encaissementRepository
                                .findActiveByTrackingId(request.encaissementSourceTrackingId())
                                .orElseThrow(() -> new RessourceNotFoundException("Encaissement source introuvable"));

                if (!enc.getSinistre().getSinistreTrackingId().equals(p.getSinistre().getSinistreTrackingId())) {
                        throw new BadRequestException(
                                        "Encaissement source incompatible : sinistre différent");
                }

                BigDecimal dejaImpute = remboursementRepository.sumMontantByEncaissement(enc.getHistoriqueId());
                BigDecimal soldeDispo = enc.getMontantCheque().subtract(dejaImpute);
                if (request.montant().compareTo(soldeDispo) > 0) {
                        throw new BadRequestException(
                                        "Le montant à rembourser dépasse le solde disponible sur cet encaissement ("
                                                        + soldeDispo + ")");
                }

                BigDecimal dejaRembourse = remboursementRepository.sumMontantByPrefinancement(p.getHistoriqueId());
                BigDecimal reste = p.getMontantPrefinance().subtract(dejaRembourse);
                if (request.montant().compareTo(reste) > 0) {
                        throw new BadRequestException(
                                        "Le montant à rembourser dépasse le reste à rembourser ("
                                                        + reste + ")");
                }

                // Écriture comptable REMB_PREFINANCEMENT — désactivée temporairement
                // var ecritureResp = comptabiliteService.genererEcritureAuto(
                // TypeTransactionComptable.REMB_PREFINANCEMENT,
                // p.getSinistre().getSinistreTrackingId(),
                // request.montant(),
                // "Remb. préfinancement " + p.getNumeroPrefinancement(),
                // loginAuteur);
                // EcritureComptable ecriture = ecritureRepository
                // .findByEcritureTrackingId(ecritureResp.ecritureTrackingId())
                // .orElseThrow(() -> new IllegalStateException(
                // "Écriture REMB_PREFINANCEMENT introuvable"));

                PrefinancementRemboursement r = PrefinancementRemboursement.builder()
                                .remboursementTrackingId(UUID.randomUUID())
                                .prefinancement(p)
                                .encaissementSource(enc)
                                .montant(request.montant())
                                .dateRemboursement(request.dateRemboursement())
                                .validePar(resolveUtilisateur(loginAuteur))
                                // .ecritureComptable(ecriture)
                                .createdBy(loginAuteur)
                                .activeData(true)
                                .deletedData(false)
                                .fromTable(TypeTable.PREFINANCEMENT)
                                .build();
                remboursementRepository.save(r);

                BigDecimal totalApres = dejaRembourse.add(request.montant());
                boolean total = totalApres.compareTo(p.getMontantPrefinance()) >= 0;
                p.setStatut(total ? StatutPrefinancement.REMBOURSE_TOTAL : StatutPrefinancement.REMBOURSE_PARTIEL);
                p.setDateRemboursement(request.dateRemboursement());
                p.setMontantRembourse(totalApres);
                p.setEstRembourse(total);
                p.setUpdatedBy(loginAuteur);
                Prefinancement saved = prefinancementRepository.save(p);

                log.info("Préfinancement {} : remboursement {} de {} ({}) → statut={}",
                                id, r.getRemboursementTrackingId(), request.montant(),
                                enc.getNumeroCheque(), saved.getStatut());

                return DataResponse.success("Remboursement enregistré", mapper.toDetailResponse(saved));
        }

        /**
         * Vue financière enrichie d'un sinistre avec détails par encaissement et par
         * préfinancement. Consommée par EncaissementImputationPicker côté frontend.
         */
        @Override
        @Transactional(readOnly = true)
        public DataResponse<CouvertureSinistreResponse> getCouvertureSinistre(UUID sinistreTrackingId) {
                BigDecimal totalEnc = encaissementRepository.sumMontantActifBySinistre(sinistreTrackingId);
                BigDecimal totalPref = prefinancementRepository.sumMontantActifBySinistre(sinistreTrackingId);
                BigDecimal totalPay = paiementRepository.sumMontantActifBySinistre(sinistreTrackingId);
                BigDecimal soldeNet = totalEnc.subtract(totalPref).subtract(totalPay);

                boolean hasEnc = encaissementRepository.existsActifNonAnnuleBySinistre(sinistreTrackingId);
                boolean hasPref = prefinancementRepository.existsActifBySinistre(sinistreTrackingId);
                boolean regleAOk = hasEnc || hasPref;
                boolean regleBOk = encaissementRepository.existsEncaisseBySinistre(sinistreTrackingId) || hasPref;
                boolean regleCOk = soldeNet.compareTo(BigDecimal.ZERO) >= 0;

                String message = null;
                if (!regleAOk)
                        message = "Aucun encaissement ni préfinancement actif sur ce sinistre.";
                else if (!regleBOk)
                        message = "Aucun chèque crédité en banque (et pas de préfinancement validé).";
                else if (!regleCOk)
                        message = "Couverture insuffisante : reste à couvrir " + soldeNet.abs() + " FCFA.";

                List<CouvertureSinistreResponse.EncaissementResumeInfo> encaissementsInfo =
                        encaissementRepository.findActifsBySinistre(sinistreTrackingId).stream()
                                .map(e -> new CouvertureSinistreResponse.EncaissementResumeInfo(
                                        e.getEncaissementTrackingId(),
                                        e.getNumeroCheque(),
                                        e.getMontantCheque(),
                                        paiementImputationService.getResteDisponible(e.getEncaissementTrackingId())))
                                .toList();

                List<CouvertureSinistreResponse.PrefiResumeInfo> prefisInfo =
                        prefinancementRepository.findActiveBySinistre(sinistreTrackingId).stream()
                                .map(p -> {
                                        BigDecimal rembourse = remboursementRepository
                                                .sumMontantByPrefinancement(p.getHistoriqueId());
                                        BigDecimal resteARembourser = p.getMontantPrefinance()
                                                .subtract(rembourse);
                                        return new CouvertureSinistreResponse.PrefiResumeInfo(
                                                p.getPrefinancementTrackingId(),
                                                p.getNumeroPrefinancement(),
                                                p.getMontantPrefinance(),
                                                resteARembourser);
                                })
                                .toList();

                CouvertureSinistreResponse response = new CouvertureSinistreResponse(
                        sinistreTrackingId,
                        totalEnc,
                        totalPref,
                        totalPay,
                        soldeNet,
                        regleAOk, regleBOk, regleCOk,
                        message,
                        encaissementsInfo,
                        prefisInfo);

                return DataResponse.success("Couverture financière calculée", response);
        }

        /**
         * Vue financière consolidée d'un sinistre : encaissements actifs +
         * préfinancements
         * actifs + total engagé sur règlements + état des règles A/B/C.
         * Source de vérité unique consommée par EncaissementGuardService et l'UI.
         */
        @Override
        @Transactional(readOnly = true)
        public DataResponse<CouvertureFinanciereResponse> getCouvertureFinanciere(UUID sid) {
                BigDecimal totalEnc = encaissementRepository.sumMontantActifBySinistre(sid);
                BigDecimal totalPref = prefinancementRepository.sumMontantActifBySinistre(sid);
                BigDecimal totalEngageReglements = paiementRepository.sumMontantActifBySinistre(sid);
                // Cash-flow : encaissements (entrées) − préfinancements (sorties anticipées)
                // − paiements (engagements actuels). Pas d'addition prefi+enc.
                BigDecimal soldeNet = totalEnc.subtract(totalPref).subtract(totalEngageReglements);
                // totalDispo = total des entrées de trésorerie réelles (encaissements seuls)
                BigDecimal totalDispo = totalEnc;

                boolean hasEnc = encaissementRepository.existsActifNonAnnuleBySinistre(sid);
                boolean hasPref = prefinancementRepository.existsActifBySinistre(sid);
                boolean regleAOk = hasEnc || hasPref;
                boolean regleBOk = encaissementRepository.existsEncaisseBySinistre(sid) || hasPref;
                boolean regleCOk = soldeNet.compareTo(BigDecimal.ZERO) >= 0;

                String message = null;
                if (!regleAOk)
                        message = "Aucun encaissement ni préfinancement actif sur ce sinistre.";
                else if (!regleBOk)
                        message = "Aucun chèque crédité en banque (et pas de préfinancement validé).";
                else if (!regleCOk)
                        message = "Couverture insuffisante : reste à couvrir " + soldeNet.abs() + " FCFA.";

                return DataResponse.success(new CouvertureFinanciereResponse(
                                totalEnc, totalPref, totalDispo, totalEngageReglements, soldeNet,
                                regleAOk, regleBOk, regleCOk, message));
        }
}
