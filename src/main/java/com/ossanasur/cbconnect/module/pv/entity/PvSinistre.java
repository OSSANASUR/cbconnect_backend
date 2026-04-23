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
import java.time.LocalDateTime;
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
    @Column(name = "detail_provenance", length = 256)
    private String detailProvenance;
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
    @Column(name = "ossan_ged_document_tracking_id")
    private java.util.UUID ossanGedDocumentTrackingId;
    @Column(name = "ossan_ged_task_id", length = 64)
    private String ossanGedTaskId;
    @Column(name = "ossan_ged_indexation_statut", length = 30)
    private String ossanGedIndexationStatut;
    @Column(name = "ossan_ged_indexation_message", length = 1000)
    private String ossanGedIndexationMessage;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entite_constat_id", nullable = false)
    private EntiteConstat entiteConstat;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sinistre_id")
    private Sinistre sinistre;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enregistre_par_id", nullable = false)
    private Utilisateur enregistrePar;

    // ─── Stockage local en attendant la mise en service de la GED ───
    @Column(name = "document_local_path", length = 512)
    private String documentLocalPath;

    @Column(name = "document_nom_fichier", length = 255)
    private String documentNomFichier;

    @Column(name = "document_mime_type", length = 100)
    private String documentMimeType;

    @Column(name = "document_taille")
    private Long documentTaille;

    @Column(name = "document_uploaded_at")
    private LocalDateTime documentUploadedAt;

    @PrePersist
    @PreUpdate
    public void calculerCompletude() {
        this.estComplet = "OK".equals(aCirconstances) && "OK".equals(aAuditions) && "OK".equals(aCroquis);
    }
}
