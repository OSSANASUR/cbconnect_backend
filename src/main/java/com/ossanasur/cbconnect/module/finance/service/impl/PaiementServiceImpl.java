package com.ossanasur.cbconnect.module.finance.service.impl;

import com.ossanasur.cbconnect.common.enums.StatutEcritureComptable;
import com.ossanasur.cbconnect.common.enums.StatutLotReglement;
import com.ossanasur.cbconnect.common.enums.StatutPaiement;
import com.ossanasur.cbconnect.common.enums.TypeOperationFinanciere;
import com.ossanasur.cbconnect.common.enums.TypeTable;
import com.ossanasur.cbconnect.common.enums.TypeTransactionComptable;
import com.ossanasur.cbconnect.module.finance.entity.LotReglement;
import com.ossanasur.cbconnect.module.finance.repository.LotReglementRepository;
import com.ossanasur.cbconnect.exception.BadRequestException;
import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.module.auth.entity.Organisme;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.module.auth.repository.OrganismeRepository;
import com.ossanasur.cbconnect.module.auth.repository.UtilisateurRepository;
import com.ossanasur.cbconnect.module.comptabilite.entity.EcritureComptable;
import com.ossanasur.cbconnect.module.comptabilite.repository.EcritureComptableRepository;
import com.ossanasur.cbconnect.module.comptabilite.service.ComptabiliteService;
import com.ossanasur.cbconnect.common.enums.TypeMotif;
import com.ossanasur.cbconnect.module.expertise.entity.Expert;
import com.ossanasur.cbconnect.module.expertise.repository.ExpertRepository;
import com.ossanasur.cbconnect.module.finance.dto.request.AnnulerPaiementRequest;
import com.ossanasur.cbconnect.module.finance.dto.request.PaiementCreateRequest;
import com.ossanasur.cbconnect.module.finance.dto.request.ReglementComptableRequest;
import com.ossanasur.cbconnect.module.finance.dto.response.PaiementDetailResponse;
import com.ossanasur.cbconnect.module.finance.dto.response.PaiementResponse;
import com.ossanasur.cbconnect.module.finance.entity.Encaissement;
import com.ossanasur.cbconnect.module.finance.entity.Paiement;
import com.ossanasur.cbconnect.module.finance.mapper.PaiementMapper;
import com.ossanasur.cbconnect.module.finance.repository.EncaissementRepository;
import com.ossanasur.cbconnect.module.finance.repository.PaiementRepository;
import com.ossanasur.cbconnect.module.finance.service.EncaissementGuardService;
import com.ossanasur.cbconnect.module.finance.service.NumeroOperationGenerator;
import com.ossanasur.cbconnect.module.finance.service.PaiementImputationService;
import com.ossanasur.cbconnect.module.finance.service.PaiementService;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import com.ossanasur.cbconnect.module.sinistre.entity.Victime;
import com.ossanasur.cbconnect.module.sinistre.repository.SinistreRepository;
import com.ossanasur.cbconnect.module.sinistre.repository.VictimeRepository;
import com.ossanasur.cbconnect.utils.DataResponse;
import com.ossanasur.cbconnect.utils.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        private final EncaissementGuardService guardService;
        private final NumeroOperationGenerator numeroOperationGenerator;

        private final LotReglementRepository lotReglementRepository;

        private final ParamMotifServiceImpl paramMotifService;
        private final ExpertRepository expertRepository;
        private final PaiementBeneficiaireValidator beneficiaireValidator;
        private final PaiementImputationService paiementImputationService;

        /**
         * Crée le règlement technique : bénéficiaire + montant uniquement.
         * Aucune info chèque à ce stade.
         * Statut résultant : EMIS
         */
        @Override
        @Transactional
        public DataResponse<PaiementDetailResponse> creer(PaiementCreateRequest request, String loginAuteur) {

                Sinistre sinistre = sinistreRepository.findActiveByTrackingId(request.sinistreTrackingId())
                                .orElseThrow(() -> new RessourceNotFoundException("Sinistre introuvable"));

                // RÈGLE A — un encaissement non-annulé doit exister sur le sinistre
                guardService.verifierRegleA(request.sinistreTrackingId());

                Victime victime = request.beneficiaireVictimeTrackingId() == null ? null
                                : victimeRepository.findActiveByTrackingId(request.beneficiaireVictimeTrackingId())
                                                .orElseThrow(() -> new RessourceNotFoundException(
                                                                "Victime bénéficiaire introuvable"));

                Organisme organisme = request.beneficiaireOrganismeTrackingId() == null ? null
                                : organismeRepository.findActiveByTrackingId(request.beneficiaireOrganismeTrackingId())
                                                .orElseThrow(() -> new RessourceNotFoundException(
                                                                "Organisme bénéficiaire introuvable"));

                Expert expert = request.beneficiaireExpertTrackingId() == null ? null
                                : expertRepository.findActiveByTrackingId(request.beneficiaireExpertTrackingId())
                                                .orElseThrow(() -> new RessourceNotFoundException(
                                                                "Expert bénéficiaire introuvable"));

                beneficiaireValidator.valider(request.categorie(), sinistre, victime, organisme, expert);

                String motifLibelle = request.motif();

                Paiement paiement = mapper.toNewEntity(request, sinistre, victime, organisme, expert, motifLibelle,
                                loginAuteur);
                // statut EMIS, numeroCheque null, ecritureComptable null
                Paiement saved = persisterPaiementAvecNumero(
                                paiement, TypeOperationFinanciere.REGLEMENT_TECHNIQUE);

                if (request.imputations() != null && !request.imputations().isEmpty()) {
                        paiementImputationService.creerImputations(saved, request.imputations(), loginAuteur);
                }

                log.info("Règlement technique créé {} (sinistre={}, montant={}, bénéficiaire={})",
                                saved.getPaiementTrackingId(), sinistre.getSinistreTrackingId(),
                                request.montant(), request.beneficiaire());

                return DataResponse.created("Règlement technique enregistré", mapper.toDetailResponse(saved));
        }

        /**
         * Valide le règlement technique.
         * Génère l'Ordre de Dépense (document — impression gérée côté front).
         * Statut résultant : REGLEMENT_TECHNIQUE_VALIDE
         * Précondition : statut == EMIS
         */
        @Override
        @Transactional
        public DataResponse<PaiementDetailResponse> validerTechnique(UUID paiementTrackingId, String loginAuteur) {

                Paiement paiement = findActiveOrThrow(paiementTrackingId);

                if (paiement.getStatut() != StatutPaiement.EMIS) {
                        throw new BadRequestException(
                                        "Seul un règlement EMIS peut être validé techniquement (statut actuel : "
                                                        + paiement.getStatut() + ")");
                }

                paiement.setStatut(StatutPaiement.REGLEMENT_TECHNIQUE_VALIDE);
                paiement.setUpdatedBy(loginAuteur);
                Paiement saved = paiementRepository.save(paiement);

                log.info("Règlement technique validé {} → REGLEMENT_TECHNIQUE_VALIDE", paiementTrackingId);

                return DataResponse.success("Règlement technique validé — Ordre de Dépense disponible",
                                mapper.toDetailResponse(saved));
        }

        /**
         * Saisit les informations comptables : chèque, banque, dates.
         * Mode paiement = CHEQUE en dur (seul mode supporté pour l'instant).
         * Statut résultant : REGLEMENT_COMPTABLE_VALIDE
         * Précondition : statut == REGLEMENT_TECHNIQUE_VALIDE
         */
        @Override
        @Transactional
        public DataResponse<PaiementDetailResponse> saisirReglementComptable(UUID paiementTrackingId,
                        ReglementComptableRequest request, String loginAuteur) {

                Paiement parent = findActiveOrThrow(paiementTrackingId);

                // RÈGLE B — règlement legacy (reprise historique) bypassé
                if (!parent.isRepriseHistorique()) {
                        guardService.verifierRegleB(parent.getSinistre().getSinistreTrackingId());
                }

                if (parent.getStatut() != StatutPaiement.REGLEMENT_TECHNIQUE_VALIDE) {
                        throw new BadRequestException(
                                        "Le règlement comptable ne peut être saisi qu'après validation technique "
                                                        + "(statut actuel : " + parent.getStatut() + ")");
                }

                // Garde-fou applicatif : un RT n'accepte qu'un seul RC actif
                boolean hasActiveRc = paiementRepository.existsActiveRcForParent(
                                parent.getPaiementTrackingId().toString());
                if (hasActiveRc) {
                        throw new BadRequestException(
                                        "Un règlement comptable actif existe déjà pour ce règlement technique. "
                                                        + "Annulez-le avant d'en saisir un nouveau.");
                }

                if (paiementRepository.existsByNumeroChequeEmisAndActiveDataTrueAndDeletedDataFalse(
                                request.numeroChequeEmis())) {
                        throw new BadRequestException("Le chèque n° " + request.numeroChequeEmis()
                                        + " est déjà enregistré sur un autre règlement");
                }

                // Nouvelle ligne = copie du règlement technique + nouveaux champs comptables
                // L'ancienne ligne reste inchangée (historique)
                Paiement reglementComptable = Paiement.builder()
                                .paiementTrackingId(UUID.randomUUID())
                                // copie des champs du règlement technique parent
                                .sinistre(parent.getSinistre())
                                .beneficiaire(parent.getBeneficiaire())
                                .beneficiaireVictime(parent.getBeneficiaireVictime())
                                .beneficiaireOrganisme(parent.getBeneficiaireOrganisme())
                                .montant(parent.getMontant())
                                .dateEmission(parent.getDateEmission())
                                .repriseHistorique(parent.isRepriseHistorique())
                                // nouveaux champs comptables
                                .banqueCheque(request.banqueCheque())
                                .numeroChequeEmis(request.numeroChequeEmis())
                                .dateEmission(request.dateEmissionReglement())
                                .dateEmissionCheque(request.dateEmissionCheque())
                                .modePaiement("CHEQUE")
                                .statut(StatutPaiement.REGLEMENT_COMPTABLE_VALIDE)
                                .categorie(parent.getCategorie())
                                .motif(parent.getMotif())
                                .beneficiaireExpert(parent.getBeneficiaireExpert())
                                // lien vers le règlement technique parent
                                .parentCodeId(parent.getPaiementTrackingId().toString())
                                // audit
                                .createdAt(LocalDateTime.now())
                                .createdBy(loginAuteur)
                                .activeData(true)
                                .deletedData(false)
                                .fromTable(TypeTable.PAIEMENT)
                                .build();

                Paiement saved = persisterPaiementAvecNumero(
                                reglementComptable, TypeOperationFinanciere.REGLEMENT_COMPTABLE);

                log.info("Règlement comptable créé {} (parent={}, chèque={}, banque={})",
                                saved.getPaiementTrackingId(), paiementTrackingId,
                                request.numeroChequeEmis(), request.banqueCheque());

                return DataResponse.success("Règlement comptable enregistré", mapper.toDetailResponse(saved));
        }

        /**
         * Valide le règlement comptable.
         * Génère l'écriture comptable (PAIEMENT_VICTIME) + la Quittance d'indemnité.
         * Statut résultant : PAYE
         * Précondition : statut == REGLEMENT_COMPTABLE_VALIDE
         */
        @Override
        @Transactional
        public DataResponse<PaiementDetailResponse> validerComptable(UUID paiementTrackingId, String loginAuteur) {

                Paiement paiement = findActiveOrThrow(paiementTrackingId);

                // RÈGLE C — règlement legacy bypassé. montant=ZERO car déjà compté dans Σ
                // engagé.
                if (!paiement.isRepriseHistorique()) {
                        guardService.verifierRegleC(
                                        paiement.getSinistre().getSinistreTrackingId(),
                                        java.math.BigDecimal.ZERO);
                }

                if (paiement.getStatut() != StatutPaiement.REGLEMENT_COMPTABLE_VALIDE) {
                        throw new BadRequestException(
                                        "Seul un règlement en REGLEMENT_COMPTABLE_VALIDE peut être validé comptablement "
                                                        + "(statut actuel : " + paiement.getStatut() + ")");
                }

                // Génération de l'écriture comptable au moment du paiement effectif
                // var ecritureResp = comptabiliteService.genererEcritureAuto(
                // TypeTransactionComptable.PAIEMENT_VICTIME,
                // paiement.getSinistre().getSinistreTrackingId(),
                // paiement.getMontant(),
                // paiement.getNumeroChequeEmis(),
                // loginAuteur);

                // EcritureComptable ecriture = ecritureRepository
                // .findByEcritureTrackingId(ecritureResp.ecritureTrackingId())
                // .orElseThrow(() -> new IllegalStateException(
                // "Écriture comptable générée introuvable : " +
                // ecritureResp.ecritureTrackingId()));

                // paiement.setEcritureComptable(ecriture);
                paiement.setStatut(StatutPaiement.PAYE);
                paiement.setDatePaiement(LocalDate.now());
                paiement.setUpdatedBy(loginAuteur);

                Paiement saved = paiementRepository.save(paiement);

                // log.info("Règlement comptable validé {} → PAYE (écriture={})",
                // paiementTrackingId, ecriture.getNumeroEcriture());

                return DataResponse.success("Règlement validé et payé — Quittance disponible",
                                mapper.toDetailResponse(saved));
        }

        /**
         * Annule un règlement à n'importe quelle étape avant PAYE.
         * Si une écriture comptable existe et est VALIDEE → contre-écriture
         * automatique.
         * Précondition : statut != ANNULE et != PAYE
         */
        @Override
        @Transactional
        public DataResponse<PaiementDetailResponse> annuler(UUID paiementTrackingId,
                        AnnulerPaiementRequest request, String loginAuteur) {

                Paiement parent = findActiveOrThrow(paiementTrackingId);

                String motifLibelle = request.motif();

                // Garde-fou : un règlement déjà annulé (ligne AN existante pointant vers lui)
                // ne peut pas l'être à nouveau
                if (paiementRepository.existsActiveAnnulationFor(parent.getPaiementTrackingId().toString())) {
                        throw new BadRequestException("Ce règlement a déjà été annulé.");
                }

                if (parent.getStatut() == StatutPaiement.ANNULE) {
                        throw new BadRequestException("Ce règlement est déjà annulé");
                }
                if (parent.getStatut() == StatutPaiement.PAYE) {
                        throw new BadRequestException(
                                        "Un règlement PAYÉ ne peut pas être annulé directement. "
                                                        + "Contactez le service comptable.");
                }

                // Contre-écriture si écriture comptable déjà générée et validée
                EcritureComptable ecriture = parent.getEcritureComptable();
                if (ecriture != null && ecriture.getStatut() == StatutEcritureComptable.VALIDEE) {
                        comptabiliteService.genererEcritureAuto(
                                        TypeTransactionComptable.CONTRA_ECRITURE,
                                        parent.getSinistre().getSinistreTrackingId(),
                                        parent.getMontant(),
                                        "Annulation règlement " + parent.getNumeroChequeEmis(),
                                        loginAuteur);
                        ecriture.setStatut(StatutEcritureComptable.ANNULEE);
                        ecritureRepository.save(ecriture);
                }

                // Nouvelle ligne = copie du règlement à annuler + statut ANNULE
                // L'ancienne ligne reste inchangée (historique)
                Paiement annulation = Paiement.builder()
                                .paiementTrackingId(UUID.randomUUID())
                                // copie de tous les champs métier du règlement parent
                                .sinistre(parent.getSinistre())
                                .beneficiaire(parent.getBeneficiaire())
                                .beneficiaireVictime(parent.getBeneficiaireVictime())
                                .beneficiaireOrganisme(parent.getBeneficiaireOrganisme())
                                .montant(parent.getMontant())
                                .modePaiement(parent.getModePaiement())
                                .numeroChequeEmis(parent.getNumeroChequeEmis())
                                .banqueCheque(parent.getBanqueCheque())
                                .dateEmission(parent.getDateEmission())
                                .dateEmissionCheque(parent.getDateEmissionCheque())
                                .datePaiement(parent.getDatePaiement())
                                .repriseHistorique(parent.isRepriseHistorique())
                                // champs propres à l'annulation
                                .categorie(parent.getCategorie())
                                .motif(motifLibelle)
                                .statut(StatutPaiement.ANNULE)
                                .motifAnnulation(request.motifAnnulation())
                                .beneficiaireExpert(parent.getBeneficiaireExpert())
                                .annulePar(resolveUtilisateur(loginAuteur))
                                // lien vers le règlement annulé
                                .parentCodeId(parent.getPaiementTrackingId().toString())
                                // audit
                                .createdAt(LocalDateTime.now())
                                .createdBy(loginAuteur)
                                .activeData(true)
                                .deletedData(false)
                                .fromTable(TypeTable.PAIEMENT)
                                .build();

                Paiement saved = persisterPaiementAvecNumero(
                                annulation, TypeOperationFinanciere.ANNULATION_REGLEMENT);

                // Contre-passage des imputations du RT parent — libère les fonds
                // alloués sur les encaissements (cf. plan imputation-encaissement Task C6).
                paiementImputationService.contrePasserImputations(parent, saved, loginAuteur);

                log.info("Règlement {} annulé par {} (motif={}, nouvelle ligne={})",
                                paiementTrackingId, loginAuteur, request.motifAnnulation(),
                                saved.getPaiementTrackingId());

                LotReglement lot = parent.getLotReglement();
                if (lot != null && lot.getStatut() != StatutLotReglement.PARTIELLEMENT_ANNULE) {
                        lot.setStatut(StatutLotReglement.PARTIELLEMENT_ANNULE);
                        lot.setUpdatedBy(loginAuteur);
                        lotReglementRepository.save(lot);
                        log.info("Lot {} basculé en PARTIELLEMENT_ANNULE suite à annulation de {}",
                                        lot.getLotTrackingId(), parent.getNumeroPaiement());
                }

                return DataResponse.success("Règlement annulé", mapper.toDetailResponse(saved));
        }

        @Override
        @Transactional
        public DataResponse<PaiementDetailResponse> lierEncaissement(UUID paiementTrackingId,
                        UUID encaissementTrackingId, String loginAuteur) {

                Paiement paiement = findActiveOrThrow(paiementTrackingId);

                if (paiement.getStatut() != StatutPaiement.PAYE) {
                        throw new BadRequestException("Le rapprochement n'est possible que sur un règlement PAYÉ");
                }

                Encaissement encaissement = encaissementRepository
                                .findActiveByTrackingId(encaissementTrackingId)
                                .orElseThrow(() -> new RessourceNotFoundException("Encaissement introuvable"));

                boolean dejaLie = paiement.getEncaissements().stream()
                                .anyMatch(e -> e.getEncaissementTrackingId().equals(encaissementTrackingId));
                if (dejaLie) {
                        throw new BadRequestException("Cet encaissement est déjà rapproché à ce règlement");
                }

                paiement.getEncaissements().add(encaissement);
                paiement.setUpdatedBy(loginAuteur);
                return DataResponse.success("Encaissement rapproché",
                                mapper.toDetailResponse(paiementRepository.save(paiement)));
        }

        @Override
        @Transactional
        public DataResponse<PaiementDetailResponse> delierEncaissement(UUID paiementTrackingId,
                        UUID encaissementTrackingId, String loginAuteur) {

                Paiement paiement = findActiveOrThrow(paiementTrackingId);

                boolean removed = paiement.getEncaissements()
                                .removeIf(e -> e.getEncaissementTrackingId().equals(encaissementTrackingId));
                if (!removed) {
                        throw new RessourceNotFoundException("Encaissement non rattaché à ce règlement");
                }

                paiement.setUpdatedBy(loginAuteur);
                return DataResponse.success("Encaissement détaché",
                                mapper.toDetailResponse(paiementRepository.save(paiement)));
        }

        @Override
        @Transactional(readOnly = true)
        public DataResponse<PaiementDetailResponse> getByTrackingId(UUID paiementTrackingId) {
                return DataResponse.success(mapper.toDetailResponse(findActiveOrThrow(paiementTrackingId)));
        }

        @Override
        @Transactional(readOnly = true)
        public DataResponse<List<PaiementResponse>> getBySinistre(UUID sinistreTrackingId) {
                List<PaiementResponse> liste = paiementRepository.findBySinistre(sinistreTrackingId)
                                .stream().map(mapper::toResponse).toList();
                return DataResponse.success(liste);
        }

        @Override
        @Transactional(readOnly = true)
        public PaginatedResponse<PaiementResponse> rechercher(StatutPaiement statut,
                        LocalDate dateDebut, LocalDate dateFin, UUID sinistreTrackingId, int page, int size) {
                var pageResult = paiementRepository.rechercherPaiements(
                                statut == null ? null : statut.name(),
                                dateDebut, dateFin, sinistreTrackingId,
                                PageRequest.of(page, size))
                                .map(mapper::toResponse);
                return PaginatedResponse.fromPage(pageResult, "Paiements");
        }

        private Paiement findActiveOrThrow(UUID id) {
                return paiementRepository.findActiveByTrackingId(id)
                                .orElseThrow(() -> new RessourceNotFoundException("Règlement introuvable"));
        }

        private Utilisateur resolveUtilisateur(String login) {
                return utilisateurRepository.findByEmailOrUsername(login, login).orElse(null);
        }

        /**
         * Persiste un Paiement après lui avoir affecté un numéro d'opération unique.
         * Retry applicatif sur DataIntegrityViolationException (collision de l'index
         * unique sur numero_operation) — max 5 tentatives.
         *
         * Doit être appelé dans un contexte transactionnel actif (le caller porte la
         * 
         * @Transactional). saveAndFlush force la violation à se déclencher dans la
         *                  boucle de retry plutôt qu'au commit global.
         */
        Paiement persisterPaiementAvecNumero(Paiement paiement,
                        TypeOperationFinanciere type) {
                final int maxRetries = 5;
                DataIntegrityViolationException last = null;
                for (int attempt = 1; attempt <= maxRetries; attempt++) {
                        try {
                                String numero = numeroOperationGenerator.genererNumero(
                                                type, paiement.getSinistre());
                                paiement.setNumeroPaiement(numero);
                                return paiementRepository.saveAndFlush(paiement);
                        } catch (DataIntegrityViolationException e) {
                                last = e;
                                log.warn("Collision numero_operation (tentative {}/{}) sinistre={} type={}",
                                                attempt, maxRetries,
                                                paiement.getSinistre().getHistoriqueId(), type);
                        }
                }
                throw last;
        }
}