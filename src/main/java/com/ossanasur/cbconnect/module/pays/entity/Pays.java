package com.ossanasur.cbconnect.module.pays.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.util.UUID;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@DiscriminatorValue("PAYS")
public class Pays extends InternalHistorique {
    @Column(name = "pays_tracking_id", unique = true)
    private UUID paysTrackingId;
    @Column(unique = true, nullable = false, length = 5)
    private String codeIso;
    @Column(unique = true, nullable = false, length = 5)
    private String codeCarteBrune;
    @Column(nullable = false)
    private String libelle;
    @Column(nullable = false)
    @Builder.Default
    private BigDecimal smigMensuel = BigDecimal.ZERO;
    @Column(nullable = false, length = 10)
    private String monnaie;
    @Builder.Default
    private BigDecimal tauxChangeXof = BigDecimal.ONE;
    @Builder.Default
    private Integer ageRetraite = 60;
    @Builder.Default
    private boolean actif = true;
    @Column(name = "taux_tva", nullable = false, precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal tauxTva = new BigDecimal("0.18");
}
