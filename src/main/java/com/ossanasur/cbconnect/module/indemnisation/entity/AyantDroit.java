package com.ossanasur.cbconnect.module.indemnisation.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.LienParente;
import com.ossanasur.cbconnect.module.sinistre.entity.Victime;
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
@DiscriminatorValue("AYANT_DROIT")
@Table(name = "ayant_droit")
public class AyantDroit extends InternalHistorique {
    @Column(name = "ayant_droit_tracking_id", unique = true)
    private UUID ayantDroitTrackingId;
    @Column(nullable = false)
    private String nom;
    @Column(nullable = false)
    private String prenoms;
    @Column(nullable = false)
    private LocalDate dateNaissance;
    @Column(nullable = false, length = 1)
    private String sexe;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LienParente lien;
    @Builder.Default
    private boolean estOrphelinDouble = false;
    @Builder.Default
    private boolean poursuiteEtudes = false;
    @Builder.Default
    private BigDecimal montantPe = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal montantPm = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal montantTotal = BigDecimal.ZERO;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "victime_id", nullable = false)
    private Victime victime;
}
