package com.ossanasur.cbconnect.module.ged.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.TypeDocumentOssanGed;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import com.ossanasur.cbconnect.module.sinistre.entity.Victime;
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
@DiscriminatorValue("OSSAN_GED_DOCUMENT")
@Table(name = "ossan_ged_document")
public class OssanGedDocument extends InternalHistorique {
    @Column(name = "ossan_ged_document_tracking_id", unique = true)
    private UUID ossanGedDocumentTrackingId;
    @Column(name = "ossan_ged_document_id", unique = true)
    private Integer ossanGedDocumentId;
    @Column(name = "ged_task_id")
    private String gedTaskId;
    @Column(nullable = false)
    private String titre;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeDocumentOssanGed typeDocument;
    private LocalDate dateDocument;
    private String mimeType;
    private String checksum;
    @Column(name = "ossan_ged_tag_id")
    private Integer ossanGedTagId;
    @Column(name = "ossan_ged_doc_type_id")
    private Integer ossanGedDocTypeId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id", nullable = false)
    private OssanGedDossier dossier;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "victime_id")
    private Victime victime;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sinistre_id")
    private Sinistre sinistre;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploade_par_id")
    private Utilisateur uploadePar;
}
