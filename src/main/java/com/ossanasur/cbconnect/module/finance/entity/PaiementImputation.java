package com.ossanasur.cbconnect.module.finance.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Trace une imputation explicite d'un paiement (RT) sur un encaissement.
 * Pattern contre-passation : montant_impute signé (+création, −annulation).
 * Une ligne négative est rattachée à un Paiement.statut = ANNULE et référence
 * l'imputation d'origine via imputation_origine_id.
 */
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@Table(name = "paiement_imputation")
public class PaiementImputation extends InternalHistorique {

    @Column(name = "imputation_tracking_id", unique = true, nullable = false)
    private UUID imputationTrackingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paiement_id", nullable = false)
    private Paiement paiement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encaissement_id", nullable = false)
    private Encaissement encaissement;

    @Column(name = "montant_impute", nullable = false, precision = 15, scale = 2)
    private BigDecimal montantImpute;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imputation_origine_id")
    private PaiementImputation imputationOrigine;
}
