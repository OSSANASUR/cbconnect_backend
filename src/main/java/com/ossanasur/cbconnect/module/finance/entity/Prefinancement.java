package com.ossanasur.cbconnect.module.finance.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.StatutPrefinancement;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.module.comptabilite.entity.EcritureComptable;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@DiscriminatorValue("PREFINANCEMENT")
public class Prefinancement extends InternalHistorique {
    @Column(name = "prefinancement_tracking_id", unique = true)
    private UUID prefinancementTrackingId;

    @Column(name = "numero_operation", length = 30, nullable = false, unique = true, updatable = false)
    private String numeroPrefinancement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private StatutPrefinancement statut = StatutPrefinancement.DEMANDE;

    @Column(name = "motif_demande", length = 500)
    private String motifDemande;

    @Column(name = "motif_annulation", length = 500)
    private String motifAnnulation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "valide_par_id")
    private Utilisateur validePar;

    @Column(name = "date_validation")
    private LocalDateTime dateValidation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annule_par_id")
    private Utilisateur annulePar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ecriture_comptable_id")
    private EcritureComptable ecritureComptable;

    @Column(nullable = false)
    private BigDecimal montantPrefinance;

    @Column(nullable = false)
    private LocalDate datePrefinancement;

    private LocalDate dateRemboursement;

    @Builder.Default
    private boolean estRembourse = false;

    @Builder.Default
    private BigDecimal montantRembourse = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sinistre_id", nullable = false)
    private Sinistre sinistre;
}
