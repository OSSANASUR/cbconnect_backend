package com.ossanasur.cbconnect.module.finance.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.module.comptabilite.entity.EcritureComptable;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Trace une opération de remboursement appliquée à un Préfinancement.
 * Un préfinancement peut être remboursé en plusieurs fois (par plusieurs
 * encaissements) — chaque imputation est une ligne distincte ici.
 */
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@Table(name = "prefinancement_remboursement")
public class PrefinancementRemboursement extends InternalHistorique {

    @Column(name = "remboursement_tracking_id", unique = true)
    private UUID remboursementTrackingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prefinancement_id", nullable = false)
    private Prefinancement prefinancement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encaissement_source_id", nullable = false)
    private Encaissement encaissementSource;

    @Column(nullable = false)
    private BigDecimal montant;

    @Column(name = "date_remboursement", nullable = false)
    private LocalDate dateRemboursement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "valide_par_id")
    private Utilisateur validePar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ecriture_comptable_id")
    private EcritureComptable ecritureComptable;
}
