package com.ossanasur.cbconnect.module.expertise.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.TauxRetenue;
import com.ossanasur.cbconnect.common.enums.TypeExpert;
import com.ossanasur.cbconnect.module.pays.entity.Pays;
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
@DiscriminatorValue("EXPERT")
public class Expert extends InternalHistorique {

    @Column(name = "expert_tracking_id", unique = true)
    private UUID expertTrackingId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeExpert typeExpert;

    @Column(nullable = false)
    private String nomComplet;

    private String specialite;
    private String nif;

    /** Email — utilisé pour l'envoi optionnel de la note de mission */
    @Column(length = 200)
    private String email;

    /** Téléphone */
    @Column(length = 30)
    private String telephone;

    @Enumerated(EnumType.STRING)
    @Column(name = "taux_retenue", nullable = false, length = 30)
    @Builder.Default
    private TauxRetenue tauxRetenue = TauxRetenue.CINQ_POURCENT;

    @Column(name = "mont_expertise", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal montExpertise = new BigDecimal("40000");

    @Builder.Default
    private boolean actif = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pays_id")
    private Pays pays;
}