package com.ossanasur.cbconnect.module.indemnisation.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.module.sinistre.entity.Victime;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@DiscriminatorValue("OFFRE_INDEMNISATION")
@Table(name = "offre_indemnisation")
public class OffreIndemnisation extends InternalHistorique {
    @Column(name = "offre_tracking_id", unique = true)
    private UUID offreTrackingId;
    @Column(nullable = false)
    private BigDecimal smigMensuelRetenu;
    @Builder.Default
    private BigDecimal montantFraisMedicaux = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal montantItt = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal montantPrejPhysiologique = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal montantPrejEconomique = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal montantPrejMoral = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal montantTiercePersonne = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal montantPretiumDoloris = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal montantPrejEsthetique = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal montantPrejCarriere = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal montantPrejScolaire = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal montantPrejLeses = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal montantFraisFuneraires = BigDecimal.ZERO;
    @Column(nullable = false)
    private BigDecimal totalBrut;
    @Column(nullable = false)
    @Builder.Default
    private BigDecimal tauxPartageRc = new BigDecimal("100");
    @Column(nullable = false)
    private BigDecimal totalNet;
    @Builder.Default
    private BigDecimal fraisGestion = BigDecimal.ZERO;
    @Column(nullable = false)
    private BigDecimal montantTotalOffre;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "detail_calcul_json", columnDefinition = "jsonb")
    private String detailCalculJson;
    private LocalDateTime dateValidation;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "victime_id", nullable = false)
    private Victime victime;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "valide_par_id")
    private Utilisateur validePar;
}