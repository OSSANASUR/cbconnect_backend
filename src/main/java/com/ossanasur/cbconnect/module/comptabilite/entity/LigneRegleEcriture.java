package com.ossanasur.cbconnect.module.comptabilite.entity;
import jakarta.persistence.*;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name="ligne_regle_ecriture")
public class LigneRegleEcriture {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="regle_id",nullable=false) private RegleEcriture regle;
    @ManyToOne @JoinColumn(name="compte_id",nullable=false) private PlanComptable compte;
    @Column(nullable=false) private String sensLigne; // DEBIT | CREDIT
    @Column(nullable=false) private String expressionMontant; // ex: "MONTANT_CHEQUE", "FRAIS_GESTION"
    private Integer ordre;
    private String libelleLigne;
}
