package com.ossanasur.cbconnect.module.comptabilite.entity;
import com.ossanasur.cbconnect.common.enums.TypeTransactionComptable;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name="regle_ecriture")
public class RegleEcriture {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer id;
    @Enumerated(EnumType.STRING) @Column(unique=true,nullable=false) private TypeTransactionComptable typeTransaction;
    @Column(nullable=false) private String libelle;
    private String description;
    @ManyToOne @JoinColumn(name="journal_id",nullable=false) private JournalComptable journal;
    @OneToMany(mappedBy="regle",cascade=CascadeType.ALL) private List<LigneRegleEcriture> lignes;
    @Builder.Default private boolean actif=true;
}
