package com.ossanasur.cbconnect.module.attestation.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.StatutLot;
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
@DiscriminatorValue("LOT_APPROVISIONNEMENT")
@Table(name = "lot_approvisionnement")
public class LotApprovisionnement extends InternalHistorique {
    @Column(name = "lot_tracking_id", unique = true)
    private UUID lotTrackingId;
    @Column(unique = true, nullable = false)
    private String referenceLot;
    private String nomFournisseur;
    private String numeroBonCommande;
    @Column(nullable = false)
    private Integer quantite;
    @Column(nullable = false)
    private String numeroDebutSerie;
    @Column(nullable = false)
    private String numeroFinSerie;
    @Column(nullable = false)
    private BigDecimal prixUnitaireAchat;
    @Column(nullable = false)
    private LocalDate dateCommande;
    private LocalDate dateLivraisonFournisseur;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutLot statutLot = StatutLot.LIVRE;
}
