package com.ossanasur.cbconnect.module.pv.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.SensCirculationPv;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.module.sinistre.entity.EntiteConstat;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
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
@DiscriminatorValue("PV_SINISTRE")
public class PvSinistre extends InternalHistorique {
    @Column(name = "pv_tracking_id", unique = true)
    private UUID pvTrackingId;
    @Column(nullable = false)
    private String numeroPv;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SensCirculationPv sensCirculation;
    @Column(nullable = false)
    private String lieuAccident;
    @Column(nullable = false)
    private LocalDate dateAccidentPv;
    @Column(nullable = false)
    private LocalDate dateReceptionBncb;
    private String provenance;
    private String referenceSinistreLiee;
    @Column(nullable = false, length = 10)
    @Builder.Default
    private String aCirconstances = "NEANT";
    @Column(nullable = false, length = 10)
    @Builder.Default
    private String aAuditions = "NEANT";
    @Column(nullable = false, length = 10)
    @Builder.Default
    private String aCroquis = "NEANT";
    @Builder.Default
    private boolean estComplet = false;
    private String remarques;
    @Column(name = "ossan_ged_document_id")
    private Integer ossanGedDocumentId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entite_constat_id", nullable = false)
    private EntiteConstat entiteConstat;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sinistre_id")
    private Sinistre sinistre;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enregistre_par_id", nullable = false)
    private Utilisateur enregistrePar;

    @PrePersist
    @PreUpdate
    public void calculerCompletude() {
        this.estComplet = "OK".equals(aCirconstances) && "OK".equals(aAuditions) && "OK".equals(aCroquis);
    }
}
