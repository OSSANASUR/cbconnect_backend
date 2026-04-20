package com.ossanasur.cbconnect.module.ged.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.TypeDossierOssanGed;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import com.ossanasur.cbconnect.module.sinistre.entity.Victime;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.UUID;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@DiscriminatorValue("OSSAN_GED_DOSSIER")
@Table(name = "ossan_ged_dossier")
public class OssanGedDossier extends InternalHistorique {
    @Column(name = "ossan_ged_dossier_tracking_id", unique = true)
    private UUID ossanGedDossierTrackingId;
    @Column(name = "ossan_ged_storage_path_id")
    private Integer ossanGedStoragePathId;
    @Column(name = "ossan_ged_correspondent_id")
    private Integer ossanGedCorrespondentId;
    @Column(nullable = false)
    private String cheminStockage;
    @Column(nullable = false)
    private String titre;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeDossierOssanGed typeDossier;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sinistre_id")
    private Sinistre sinistre;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "victime_id")
    private Victime victime;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_dossier_id")
    private OssanGedDossier parentDossier;
}
