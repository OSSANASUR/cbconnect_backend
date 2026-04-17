package com.ossanasur.cbconnect.module.baremes.entity;
import jakarta.persistence.*;
import lombok.*; import java.math.BigDecimal;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name="bareme_cle_repartition_265")
public class BaremeCleRepartition265 {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer id;
    @Column(unique=true,nullable=false) private String codeSituation;
    @Column(nullable=false) private String libelleSituation;
    private boolean conditionConjoint; private boolean conditionEnfant; private Integer nombreMaxEnfants;
    @Column(nullable=false,precision=5,scale=2) @Builder.Default private BigDecimal cleAscendants=BigDecimal.ZERO;
    @Column(nullable=false,precision=5,scale=2) @Builder.Default private BigDecimal cleConjoints=BigDecimal.ZERO;
    @Column(nullable=false,precision=5,scale=2) @Builder.Default private BigDecimal cleEnfants=BigDecimal.ZERO;
    @Column(nullable=false,precision=5,scale=2) @Builder.Default private BigDecimal cleOrphelinsDoubles=BigDecimal.ZERO;
    @Builder.Default private boolean actif=true;
}
