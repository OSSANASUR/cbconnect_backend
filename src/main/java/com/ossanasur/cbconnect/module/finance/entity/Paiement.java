package com.ossanasur.cbconnect.module.finance.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.CategorieReglement;
import com.ossanasur.cbconnect.common.enums.StatutPaiement;
import com.ossanasur.cbconnect.common.enums.TypePrejudice;
import com.ossanasur.cbconnect.module.auth.entity.Organisme;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.module.comptabilite.entity.EcritureComptable;
import com.ossanasur.cbconnect.module.expertise.entity.Expert;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import com.ossanasur.cbconnect.module.sinistre.entity.Victime;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@DiscriminatorValue("PAIEMENT")
public class Paiement extends InternalHistorique {

    @Column(name = "paiement_tracking_id", unique = true)
    private UUID paiementTrackingId;

    @Column(nullable = false)
    private String beneficiaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beneficiaire_victime_id")
    private Victime beneficiaireVictime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beneficiaire_organisme_id")
    private Organisme beneficiaireOrganisme;

    @Column
    private String numeroChequeEmis;

    private String banqueCheque;

    @Column(nullable = false)
    private BigDecimal montant;

    @Column
    private LocalDate dateEmissionCheque;

    @Column(nullable = false)
    private LocalDate dateEmission;

    private LocalDate datePaiement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private StatutPaiement statut = StatutPaiement.EMIS;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_prejudice", nullable = false, length = 20)
    @Builder.Default
    private TypePrejudice typePrejudice = TypePrejudice.MATERIEL;

    @Column(name = "motif_complement", length = 255)
    private String motifComplement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategorieReglement categorie;

    @Column(nullable = false, length = 150)
    private String motif;

    @Column(name = "numero_operation", length = 30, nullable = false, unique = true, updatable = false)
    private String numeroPaiement;

    private String motifAnnulation;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sinistre_id", nullable = false)
    private Sinistre sinistre;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "paiement_encaissement", joinColumns = @JoinColumn(name = "paiement_id"), inverseJoinColumns = @JoinColumn(name = "encaissement_id"))
    @Builder.Default
    private java.util.List<Encaissement> encaissements = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annule_par_id")
    private Utilisateur annulePar;

    @Column(name = "mode_paiement", length = 20)
    private String modePaiement;

    /** TRUE si importé via reprise historique */
    @Column(name = "reprise_historique")
    @Builder.Default
    private boolean repriseHistorique = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ecriture_comptable_id")
    private EcritureComptable ecritureComptable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beneficiaire_expert_id")
    private Expert beneficiaireExpert;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_reglement_id")
    private LotReglement lotReglement;

    @Column(name = "montant_ttc", precision = 15, scale = 2)
    private BigDecimal montantTtc;

    @Column(name = "montant_tva", precision = 15, scale = 2)
    private BigDecimal montantTva;

    @Column(name = "montant_taxe", precision = 15, scale = 2)
    private BigDecimal montantTaxe;

}
