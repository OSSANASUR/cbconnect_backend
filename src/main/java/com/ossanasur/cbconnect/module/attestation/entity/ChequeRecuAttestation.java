package com.ossanasur.cbconnect.module.attestation.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.StatutCheque;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@DiscriminatorValue("CHEQUE_RECU_ATTESTATION")
@Table(name = "cheque_recu_attestation")
public class ChequeRecuAttestation extends InternalHistorique {
    @Column(name = "cheque_tracking_id", unique = true)
    private UUID chequeTrackingId;
    @Column(nullable = false)
    private String numeroCheque;
    @Column(nullable = false)
    private BigDecimal montant;
    private String banqueEmettrice;
    @Column(nullable = false)
    private LocalDate dateEmission;
    private LocalDate dateReception;
    private LocalDate dateEncaissement;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutCheque statut = StatutCheque.RECU;
    private String motifAnnulation;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facture_id", nullable = false)
    private FactureAttestation facture;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annule_par_id")
    private Utilisateur annulePar;
}
