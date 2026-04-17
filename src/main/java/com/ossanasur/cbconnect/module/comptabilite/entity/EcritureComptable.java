package com.ossanasur.cbconnect.module.comptabilite.entity;
import com.ossanasur.cbconnect.common.enums.*;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal; import java.time.LocalDate; import java.time.LocalDateTime;
import java.util.List; import java.util.UUID;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name="ecriture_comptable")
public class EcritureComptable {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer id;
    @Column(unique=true,nullable=false) private UUID ecritureTrackingId;
    @Column(unique=true,nullable=false) private String numeroEcriture; // EX: ECR-2025-000123
    @Enumerated(EnumType.STRING) @Column(nullable=false) private TypeTransactionComptable typeTransaction;
    @Column(nullable=false) private LocalDate dateEcriture;
    @Column(nullable=false) private String libelle;
    @Column(nullable=false) private BigDecimal montantTotal;
    @Enumerated(EnumType.STRING) @Column(nullable=false) @Builder.Default private StatutEcritureComptable statut=StatutEcritureComptable.BROUILLON;
    private LocalDateTime dateValidation;
    private String referenceExterneId; // ID encaissement ou paiement source
    private String referenceExterneType; // ENCAISSEMENT | PAIEMENT | etc.
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="journal_id") private JournalComptable journal;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="sinistre_id") private Sinistre sinistre;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="saisi_par_id") private Utilisateur saisiPar;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="valide_par_id") private Utilisateur validePar;
    @OneToMany(mappedBy="ecriture",cascade=CascadeType.ALL) private List<LigneEcriture> lignes;
}
