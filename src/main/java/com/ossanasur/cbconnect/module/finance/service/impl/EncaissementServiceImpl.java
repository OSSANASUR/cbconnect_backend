package com.ossanasur.cbconnect.module.finance.service.impl;

import com.ossanasur.cbconnect.common.enums.*;
import com.ossanasur.cbconnect.exception.*;
import com.ossanasur.cbconnect.module.auth.entity.Organisme;
import com.ossanasur.cbconnect.module.auth.repository.OrganismeRepository;
import com.ossanasur.cbconnect.module.finance.dto.request.EncaissementRequest;
import com.ossanasur.cbconnect.module.finance.dto.response.EncaissementResponse;
import com.ossanasur.cbconnect.module.finance.entity.Encaissement;
import com.ossanasur.cbconnect.module.finance.entity.Paiement;
import com.ossanasur.cbconnect.module.finance.exception.ReglementsLiesNonAnnulesException;
import com.ossanasur.cbconnect.module.finance.mapper.EncaissementMapper;
import com.ossanasur.cbconnect.module.finance.repository.EncaissementRepository;
import com.ossanasur.cbconnect.module.finance.repository.PaiementImputationRepository;
import com.ossanasur.cbconnect.module.finance.repository.PaiementRepository;
import com.ossanasur.cbconnect.module.finance.repository.PrefinancementRemboursementRepository;
import com.ossanasur.cbconnect.module.finance.service.EncaissementService;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import com.ossanasur.cbconnect.module.sinistre.repository.SinistreRepository;
import com.ossanasur.cbconnect.utils.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EncaissementServiceImpl implements EncaissementService {
        private final EncaissementRepository encaissementRepository;
        private final SinistreRepository sinistreRepository;
        private final OrganismeRepository organismeRepository;
        private final EncaissementMapper mapper;
        private final PaiementRepository paiementRepository;
        private final PaiementImputationRepository paiementImputationRepository;
        private final PrefinancementRemboursementRepository prefiRemboursementRepository;

        @Override
        @Transactional
        public DataResponse<EncaissementResponse> create(EncaissementRequest r, String loginAuteur) {

                Sinistre sinistre = sinistreRepository.findActiveByTrackingId(r.sinistreTrackingId())
                                .orElseThrow(() -> new RessourceNotFoundException("Sinistre introuvable"));

                Organisme organisme = organismeRepository.findActiveByTrackingId(r.organismeEmetteurTrackingId())
                                .orElseThrow(() -> new RessourceNotFoundException("Organisme emetteur introuvable"));

                Organisme chequeOrdre = organismeRepository.findActiveByTrackingId(r.chequeOrdreOrganismeTrackingId())
                                .orElseThrow(() -> new RessourceNotFoundException(
                                                "Organisme bénéficiaire du chèque introuvable"));

                // Calcul automatique frais de gestion (5% si sinistre SURVENU_TOGO)
                BigDecimal fraisGestion = BigDecimal.ZERO;
                if (TypeSinistre.SURVENU_TOGO.equals(sinistre.getTypeSinistre())) {
                        fraisGestion = r.montantCheque().multiply(new BigDecimal("0.05")).setScale(2,
                                        java.math.RoundingMode.HALF_UP);
                }

                Encaissement e = mapper.toNewEntity(r, sinistre, organisme, chequeOrdre, loginAuteur, StatutCheque.RECU,
                                fraisGestion);

                return DataResponse.created("Encaissement enregistre",
                                mapper.toResponse(encaissementRepository.save(e)));
        }

        @Override
        @Transactional(readOnly = true)
        public DataResponse<EncaissementResponse> getByTrackingId(UUID id) {
                return DataResponse.success(mapper.toResponse(encaissementRepository.findActiveByTrackingId(id)
                                .orElseThrow(() -> new RessourceNotFoundException("Encaissement introuvable"))));
        }

        @Override
        @Transactional(readOnly = true)
        public DataResponse<List<EncaissementResponse>> getBySinistre(UUID sinistreId) {
                return DataResponse.success(
                                encaissementRepository.findBySinistre(sinistreId).stream().map(mapper::toResponse)
                                                .collect(Collectors.toList()));
        }

        @Override
        @Transactional
        public DataResponse<Void> encaisser(UUID id, LocalDate dateEncaissement, String loginAuteur) {
                Encaissement e = encaissementRepository.findActiveByTrackingId(id)
                                .orElseThrow(() -> new RessourceNotFoundException("Encaissement introuvable"));
                e.setStatutCheque(StatutCheque.ENCAISSE);
                e.setDateEncaissement(dateEncaissement);
                e.setUpdatedBy(loginAuteur);
                encaissementRepository.save(e);
                return DataResponse.success("Cheque encaisse", null);
        }

        @Override
        @Transactional
        public DataResponse<Void> annuler(UUID id, String motif, String loginAuteur) {
                Encaissement e = encaissementRepository.findActiveByTrackingId(id)
                                .orElseThrow(() -> new RessourceNotFoundException("Encaissement introuvable"));

                if (e.getStatutCheque() == StatutCheque.ANNULE) {
                        throw new BadRequestException("Cet encaissement est déjà annulé");
                }

                List<Paiement> bloquants = paiementRepository.findReglementsLiesNonAnnules(id);
                if (!bloquants.isEmpty()) {
                        throw new ReglementsLiesNonAnnulesException(bloquants);
                }

                // Garde imputation_encaissement (nouveau modèle) :
                // somme algébrique des imputations actives doit être nulle pour autoriser l'annulation.
                BigDecimal sumImputations = nz(paiementImputationRepository.sumImputationsByEncaissement(e.getHistoriqueId()));
                BigDecimal sumPrefi = nz(prefiRemboursementRepository.sumMontantByEncaissement(e.getHistoriqueId()));
                if (sumImputations.signum() != 0 || sumPrefi.signum() != 0) {
                        throw new BadRequestException(String.format(
                                        "Impossible d'annuler ce chèque : il finance encore des opérations actives "
                                        + "(imputations règlements : %s FCFA, remboursements préfi : %s FCFA). "
                                        + "Annulez d'abord les règlements/remboursements concernés.",
                                        sumImputations.toPlainString(), sumPrefi.toPlainString()));
                }

                e.setStatutCheque(StatutCheque.ANNULE);
                e.setMotifAnnulation(motif);
                e.setUpdatedBy(loginAuteur);
                encaissementRepository.save(e);
                return DataResponse.success("Encaissement annulé", null);
        }

        private static BigDecimal nz(BigDecimal v) {
                return v == null ? BigDecimal.ZERO : v;
        }
}
