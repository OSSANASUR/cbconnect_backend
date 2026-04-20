package com.ossanasur.cbconnect.module.attestation.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.TypeFactureAttestation;
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
@DiscriminatorValue("FACTURE_ATTESTATION")
@Table(name = "facture_attestation")
public class FactureAttestation extends InternalHistorique {
    @Column(name = "facture_attestation_tracking_id", unique = true)
    private UUID factureTrackingId;
    @Column(unique = true, nullable = false)
    private String numeroFacture;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeFactureAttestation typeFacture;
    @Column(nullable = false)
    private LocalDate dateFacture;
    @Column(nullable = false)
    private BigDecimal montantAttestation;
    @Column(nullable = false)
    private BigDecimal montantContributionFonds;
    @Column(nullable = false)
    private BigDecimal montantTotal;
    private String montantEnLettres;
    private String instructionCheque;
    private LocalDate dateEcheance;
    @Column(name = "ossan_ged_document_id")
    private Long ossanGedDocumentId;
    @Builder.Default
    private boolean annulee = false;
    private String motifAnnulation;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id", nullable = false)
    private CommandeAttestation commande;
}
