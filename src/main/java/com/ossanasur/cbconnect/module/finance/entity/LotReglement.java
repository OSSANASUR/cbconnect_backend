package com.ossanasur.cbconnect.module.finance.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.StatutLotReglement;
import com.ossanasur.cbconnect.common.enums.TauxRetenue;
import com.ossanasur.cbconnect.module.expertise.entity.Expert;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "lot_reglement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@DiscriminatorValue("LOT_REGLEMENT")
public class LotReglement extends InternalHistorique {

    @Column(name = "lot_tracking_id", unique = true)
    private UUID lotTrackingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expert_id", nullable = false)
    private Expert expert;

    @Enumerated(EnumType.STRING)
    @Column(name = "taux_retenue", nullable = false, length = 30)
    private TauxRetenue tauxRetenue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private StatutLotReglement statut;

    @Column(name = "nombre_reglements", nullable = false)
    private Integer nombreReglements;

    @Column(name = "montant_ttc_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal montantTtcTotal;

    @Column(name = "montant_tva_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal montantTvaTotal;

    @Column(name = "montant_taxe_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal montantTaxeTotal;

    @Column(name = "numero_cheque_global", length = 30)
    private String numeroChequeGlobal;

    @Column(name = "banque_cheque", length = 150)
    private String banqueCheque;

    @Column(name = "date_emission_cheque")
    private LocalDate dateEmissionCheque;
}
