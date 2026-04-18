package com.ossanasur.cbconnect.module.ged.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.TypeDocumentPaperless;
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
@DiscriminatorValue("PAPERLESS_DOCUMENT")
@Table(name = "paperless_document")
public class PaperlessDocument extends InternalHistorique {
    @Column(name = "paperless_document_tracking_id", unique = true)
    private UUID paperlessDocumentTrackingId;
    @Column(unique = true, nullable = false)
    private Integer paperlessDocumentId;
    @Column(nullable = false)
    private String titre;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeDocumentPaperless typeDocument;
    private LocalDate dateDocument;
    private String mimeType;
    private String checksum;
    private Integer paperlessTagId;
    private Integer paperlessDocTypeId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id", nullable = false)
    private PaperlessDossier dossier;
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
