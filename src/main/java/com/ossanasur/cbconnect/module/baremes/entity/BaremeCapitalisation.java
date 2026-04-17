package com.ossanasur.cbconnect.module.baremes.entity;
import jakarta.persistence.*;
import lombok.*; import java.math.BigDecimal;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name="bareme_capitalisation")
public class BaremeCapitalisation {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer id;
    @Column(nullable=false,length=5) private String typeBareme; // M100,F100,M25,F25
    @Column(nullable=false) private Integer age;
    @Column(nullable=false,precision=10,scale=4) private BigDecimal prixFrancRente;
    @Column(nullable=false,precision=5,scale=2) private BigDecimal tauxCapitalisation;
    @Column(nullable=false) private String tableMortalite;
    @Column(nullable=false) private Integer ageLimitePaiement;
    @Builder.Default private boolean actif=true;
}
