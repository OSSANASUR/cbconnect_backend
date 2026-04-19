package com.ossanasur.cbconnect.module.attestation.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDate;
import java.util.UUID;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@DiscriminatorValue("TRANCHE_LIVRAISON")
@Table(name = "tranche_livraison_attestation")
public class TrancheLivraisonAttestation extends InternalHistorique {
    @Column(name = "tranche_tracking_id", unique = true)
    private UUID trancheTrackingId;
    @Column(nullable = false)
    private String numeroDebutSerie;
    @Column(nullable = false)
    private String numeroFinSerie;
    @Column(nullable = false)
    private Integer quantiteLivree;
    @Column(nullable = false)
    private LocalDate dateLivraison;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id", nullable = false)
    private CommandeAttestation commande;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", nullable = false)
    private LotApprovisionnement lot;
}
