package com.ossanasur.cbconnect.module.delai.entity;
import com.ossanasur.cbconnect.common.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name="parametre_delai")
public class ParametreDelai {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer id;
    @Column(unique=true,nullable=false) private String codeDelai;
    @Column(nullable=false) private String libelle;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private TypeDelai typeDelai;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private CategorieActiviteDelai categorie;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private TypeSinistre typeSinistre;
    @Column(nullable=false) private BigDecimal valeur;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private UniteDelai unite;
    private String referenceJuridique;
    private BigDecimal tauxPenalitePct;
    @Column(name="seuil_alerte1_pct") private BigDecimal seuilAlerte1Pct;
    @Column(name="seuil_alerte2_pct") private BigDecimal seuilAlerte2Pct;
    @Builder.Default private boolean modifiable = true;
    @Builder.Default private boolean actif=true;
}
