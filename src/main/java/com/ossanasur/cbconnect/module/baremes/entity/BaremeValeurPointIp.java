package com.ossanasur.cbconnect.module.baremes.entity;
import jakarta.persistence.*;
import lombok.*; import java.math.BigDecimal;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name="bareme_valeur_point_ip")
public class BaremeValeurPointIp {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer id;
    @Column(nullable=false) private Integer ageMin;
    private Integer ageMax; // null = sans limite
    @Column(nullable=false,precision=5,scale=2) private BigDecimal ippMin;
    @Column(nullable=false,precision=5,scale=2) private BigDecimal ippMax;
    @Column(nullable=false) private Integer valeurPoint; // en % du SMIG annuel
    @Builder.Default private boolean actif=true;
}
