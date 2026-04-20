package com.ossanasur.cbconnect.module.expertise.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.*;
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
@DiscriminatorValue("EXPERTISE_MEDICALE")
public class ExpertiseMedicale extends InternalHistorique {
    @Column(name = "expertise_med_tracking_id", unique = true)
    private UUID expertiseMedTrackingId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeExpertise typeExpertise;
    @Column(nullable = false)
    private LocalDate dateDemande;
    private LocalDate dateRapport;
    private LocalDate dateConsolidation;
    @Builder.Default
    private BigDecimal tauxIpp = BigDecimal.ZERO;
    @Builder.Default
    private Integer dureeIttJours = 0;
    @Builder.Default
    private Integer dureeItpJours = 0;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private QualificationPretium pretiumDoloris = QualificationPretium.NEANT;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private QualificationPretium prejudiceEsthetique = QualificationPretium.NEANT;
    @Builder.Default
    private boolean necessiteTiercePersonne = false;
    @Builder.Default
    private BigDecimal honoraires = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal honorairesContreExpertise = BigDecimal.ZERO;
    @Column(name = "ossan_ged_document_id")
    private Long ossanGedDocumentId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "victime_id", nullable = false)
    private Victime victime;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expert_id")
    private Expert expert;
}
