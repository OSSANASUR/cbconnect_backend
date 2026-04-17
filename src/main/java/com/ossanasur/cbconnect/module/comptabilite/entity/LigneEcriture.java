package com.ossanasur.cbconnect.module.comptabilite.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name="ligne_ecriture")
public class LigneEcriture {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="ecriture_id",nullable=false) private EcritureComptable ecriture;
    @ManyToOne @JoinColumn(name="compte_id",nullable=false) private PlanComptable compte;
    @Column(nullable=false,length=6) private String sens; // DEBIT | CREDIT
    @Column(nullable=false) private BigDecimal montant;
    private String libelleLigne; private Integer ordre;
}
