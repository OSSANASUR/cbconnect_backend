package com.ossanasur.cbconnect.module.delai.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "parametre_systeme")
public class ParametreSysteme {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
    @Column(unique = true, nullable = false, length = 80) private String cle;
    @Column(nullable = false, length = 200) private String libelle;
    @Column(precision = 10, scale = 4) private BigDecimal valeurDecimal;
    @Column(columnDefinition = "TEXT") private String description;
    @Builder.Default private boolean actif = true;
}
