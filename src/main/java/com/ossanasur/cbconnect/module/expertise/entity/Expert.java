package com.ossanasur.cbconnect.module.expertise.entity;
import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.TypeExpert;
import com.ossanasur.cbconnect.module.pays.entity.Pays;
import jakarta.persistence.*;
import lombok.*; import lombok.experimental.SuperBuilder;
import java.io.Serializable; import java.math.BigDecimal; import java.util.UUID;
@RequiredArgsConstructor @AllArgsConstructor @Getter @Setter @SuperBuilder
@Entity @DiscriminatorValue("EXPERT")
public class Expert extends InternalHistorique implements Serializable {
    @Column(name="expert_tracking_id",unique=true) private UUID expertTrackingId;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private TypeExpert typeExpert;
    @Column(nullable=false) private String nomComplet;
    private String specialite; private String nif; private BigDecimal tauxRetenue;
    @Builder.Default private boolean actif=true;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="pays_id") private Pays pays;
}
