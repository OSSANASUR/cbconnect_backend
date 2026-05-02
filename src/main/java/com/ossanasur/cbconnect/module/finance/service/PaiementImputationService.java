package com.ossanasur.cbconnect.module.finance.service;

import com.ossanasur.cbconnect.module.finance.dto.request.ImputationRequest;
import com.ossanasur.cbconnect.module.finance.dto.response.EncaissementResteResponse;
import com.ossanasur.cbconnect.module.finance.dto.response.PaiementImputationResponse;
import com.ossanasur.cbconnect.module.finance.entity.Paiement;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PaiementImputationService {

    /** Reste disponible algébrique d'un encaissement = montant_cheque − Σ imputations actives. */
    BigDecimal getResteDisponible(UUID encaissementTrackingId);

    EncaissementResteResponse getResteDetailEncaissement(UUID encaissementTrackingId);

    List<PaiementImputationResponse> getImputationsByEncaissement(UUID encaissementTrackingId);

    List<PaiementImputationResponse> getImputationsByPaiement(UUID paiementTrackingId);

    /**
     * Crée les imputations positives pour un paiement.
     * Validations : Σ imputations == paiement.montant, reste(E) ≥ imputation pour chaque E.
     * Lock pessimiste sur les encaissements pendant la transaction.
     */
    void creerImputations(Paiement paiement, List<ImputationRequest> imputations, String createdBy);

    /**
     * Génère les contre-passages négatifs pour annuler un paiement.
     * Chaque ligne d'origine active du paiementOrigine produit une ligne négative
     * rattachée à anPaiement.
     */
    void contrePasserImputations(Paiement paiementOrigine, Paiement anPaiement, String createdBy);
}
