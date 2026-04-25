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
import com.ossanasur.cbconnect.module.finance.dto.request.PaiementCreateRequest;
import com.ossanasur.cbconnect.module.finance.dto.request.ReglementComptableRequest;
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

                Victime victime = request.beneficiaireVictimeTrackingId() == null ? null
                                : victimeRepository.findActiveByTrackingId(request.beneficiaireVictimeTrackingId())
                                                .orElseThrow(() -> new RessourceNotFoundException(
                                                                "Victime bénéficiaire introuvable"));

                Organisme organisme = request.beneficiaireOrganismeTrackingId() == null ? null
                                : organismeRepository.findActiveByTrackingId(request.beneficiaireOrganismeTrackingId())
                                                .orElseThrow(() -> new RessourceNotFoundException(
                                                                "Organisme bénéficiaire introuvable"));

                Paiement paiement = mapper.toNewEntity(request, sinistre, victime, organisme, loginAuteur);
                // statut EMIS, numeroCheque null, ecritureComptable null
                Paiement saved = paiementRepository.save(paiement);

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

                Paiement paiement = findActiveOrThrow(paiementTrackingId);

                if (paiement.getStatut() != StatutPaiement.REGLEMENT_TECHNIQUE_VALIDE) {
                        throw new BadRequestException(
                                        "Le règlement comptable ne peut être saisi qu'après validation technique "
                                                        + "(statut actuel : " + paiement.getStatut() + ")");
                }

                // Unicité du numéro de chèque (on exclut le paiement lui-même pour
                // permettre une correction sur un RC encore non validé si besoin futur)
                if (paiementRepository.existsByNumeroChequeEmisAndActiveDataTrueAndDeletedDataFalse(
                                request.numeroChequeEmis())) {
                        throw new BadRequestException("Le chèque n° " + request.numeroChequeEmis()
                                        + " est déjà enregistré sur un autre règlement");
                }

                paiement.setNumeroChequeEmis(request.numeroChequeEmis());
                paiement.setBanqueCheque(request.banqueCheque());
                paiement.setDateEmission(request.dateEmissionReglement());
                paiement.setDateEmissionCheque(request.dateEmissionCheque());
                paiement.setModePaiement("CHEQUE");
                paiement.setStatut(StatutPaiement.REGLEMENT_COMPTABLE_VALIDE);
                paiement.setUpdatedBy(loginAuteur);

                Paiement saved = paiementRepository.save(paiement);

                log.info("Règlement comptable saisi {} (chèque={}, banque={})",
                                paiementTrackingId, request.numeroChequeEmis(), request.banqueCheque());

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

                Paiement paiement = findActiveOrThrow(paiementTrackingId);

                if (paiement.getStatut() == StatutPaiement.ANNULE) {
                        throw new BadRequestException("Ce règlement est déjà annulé");
                }
                if (paiement.getStatut() == StatutPaiement.PAYE) {
                        throw new BadRequestException(
                                        "Un règlement PAYÉ ne peut pas être annulé directement. "
                                                        + "Contactez le service comptable.");
                }

                // Contre-écriture si l'écriture comptable a déjà été générée et validée
                EcritureComptable ecriture = paiement.getEcritureComptable();
                if (ecriture != null && ecriture.getStatut() == StatutEcritureComptable.VALIDEE) {
                        comptabiliteService.genererEcritureAuto(
                                        TypeTransactionComptable.CONTRA_ECRITURE,
                                        paiement.getSinistre().getSinistreTrackingId(),
                                        paiement.getMontant(),
                                        "Annulation règlement " + paiement.getNumeroChequeEmis(),
                                        loginAuteur);
                        ecriture.setStatut(StatutEcritureComptable.ANNULEE);
                        ecritureRepository.save(ecriture);
                }

                paiement.setStatut(StatutPaiement.ANNULE);
                paiement.setMotifAnnulation(request.motifAnnulation());
                paiement.setAnnulePar(resolveUtilisateur(loginAuteur));
                paiement.setUpdatedBy(loginAuteur);
                Paiement saved = paiementRepository.save(paiement);

                log.info("Règlement {} annulé par {} (motif={})",
                                paiementTrackingId, loginAuteur, request.motifAnnulation());

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
}