package com.ossanasur.cbconnect.module.baremes.entity;
import jakarta.persistence.*;
import lombok.*; import java.math.BigDecimal;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name="bareme_prejudice_moral_266")
public class BaremePrejudiceMoral266 {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer id;
    @Column(unique=true,nullable=false) private String lienParente; // CONJOINT,ENFANT_MINEUR,etc
    @Column(nullable=false,precision=6,scale=2) private BigDecimal cle; // % SMIG annuel
    private String plafondCategorie; @Builder.Default private boolean actif=true;
}
