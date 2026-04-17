package com.ossanasur.cbconnect.module.ged.entity;
import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.TypeDossierPaperless;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import com.ossanasur.cbconnect.module.sinistre.entity.Victime;
import jakarta.persistence.*;
import lombok.*; import lombok.experimental.SuperBuilder;
import java.io.Serializable; import java.util.UUID;
@RequiredArgsConstructor @AllArgsConstructor @Getter @Setter @SuperBuilder
@Entity @DiscriminatorValue("PAPERLESS_DOSSIER") @Table(name="paperless_dossier")
public class PaperlessDossier extends InternalHistorique implements Serializable {
    @Column(name="paperless_dossier_tracking_id",unique=true) private UUID paperlessDossierTrackingId;
    private Integer paperlessStoragePathId; private Integer paperlessCorrespondentId;
    @Column(nullable=false) private String cheminStockage;
    @Column(nullable=false) private String titre;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private TypeDossierPaperless typeDossier;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="sinistre_id") private Sinistre sinistre;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="victime_id") private Victime victime;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="parent_dossier_id") private PaperlessDossier parentDossier;
}
