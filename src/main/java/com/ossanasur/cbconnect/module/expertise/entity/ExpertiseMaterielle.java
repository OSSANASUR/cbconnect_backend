package com.ossanasur.cbconnect.module.expertise.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
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
@DiscriminatorValue("EXPERTISE_MATERIELLE")
public class ExpertiseMaterielle extends InternalHistorique {
    @Column(name = "expertise_ma_tracking_id", unique = true)
    private UUID expertiseMaTrackingId;
    @Column(nullable = false)
    private LocalDate dateDemande;
    private LocalDate dateRapport;
    private BigDecimal montantDevis;
    private BigDecimal montantDitExpert;
    @Builder.Default
    private BigDecimal honoraires = BigDecimal.ZERO;
    @Column(name = "ossan_ged_document_id")
    private Integer ossanGedDocumentId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sinistre_id", nullable = false)
    private Sinistre sinistre;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expert_id")
    private Expert expert;
}
