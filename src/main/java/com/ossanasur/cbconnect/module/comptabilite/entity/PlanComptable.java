package com.ossanasur.cbconnect.module.comptabilite.entity;
import com.ossanasur.cbconnect.common.enums.TypeCompteComptable;
import jakarta.persistence.*;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name="plan_comptable")
public class PlanComptable {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer id;
    @Column(unique=true,nullable=false,length=10) private String numeroCompte;
    @Column(nullable=false) private String libelleCompte;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private TypeCompteComptable typeCompte;
    private String compteParentNumero;
    @Builder.Default private boolean actif=true;
}
