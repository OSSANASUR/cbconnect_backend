package com.ossanasur.cbconnect.module.attestation.entity;
import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.StatutCommandeAttestation;
import com.ossanasur.cbconnect.module.auth.entity.Organisme;
import jakarta.persistence.*;
import lombok.*; import lombok.experimental.SuperBuilder;
import java.io.Serializable; import java.math.BigDecimal; import java.time.LocalDate; import java.util.UUID;
@RequiredArgsConstructor @AllArgsConstructor @Getter @Setter @SuperBuilder
@Entity @DiscriminatorValue("COMMANDE_ATTESTATION") @Table(name="commande_attestation")
public class CommandeAttestation extends InternalHistorique implements Serializable {
    @Column(name="commande_tracking_id",unique=true) private UUID commandeTrackingId;
    @Column(unique=true,nullable=false) private String numeroCommande;
    @Enumerated(EnumType.STRING) @Column(nullable=false) @Builder.Default private StatutCommandeAttestation statut=StatutCommandeAttestation.PROFORMA_EMISE;
    @Column(nullable=false) private Integer quantite;
    @Column(nullable=false) @Builder.Default private BigDecimal prixUnitaireVente = new BigDecimal("1075");
    @Column(nullable=false) @Builder.Default private BigDecimal tauxContributionFonds = new BigDecimal("100");
    @Column(nullable=false) private BigDecimal montantAttestation;
    @Column(nullable=false) private BigDecimal montantContributionFonds;
    @Column(nullable=false) private BigDecimal montantTotal;
    private String montantEnLettres;
    @Column(nullable=false) private LocalDate dateCommande;
    private LocalDate dateLivraisonEffective;
    private String nomBeneficiaireCheque;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="organisme_id",nullable=false) private Organisme organisme;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="lot_id") private LotApprovisionnement lot;
}
