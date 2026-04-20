package com.ossanasur.cbconnect.module.finance.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.StatutPaiement;
import com.ossanasur.cbconnect.module.auth.entity.Organisme;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import com.ossanasur.cbconnect.module.sinistre.entity.Victime;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDate;
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
    @Column(nullable = false)
    private String numeroChequeEmis;
    private String banqueCheque;
    @Column(nullable = false)
    private BigDecimal montant;
    @Column(nullable = false)
    private LocalDate dateEmission;
    private LocalDate datePaiement;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutPaiement statut = StatutPaiement.EMIS;
    private String motifAnnulation;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sinistre_id", nullable = false)
    private Sinistre sinistre;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "paiement_encaissement", joinColumns = @JoinColumn(name = "paiement_id"), inverseJoinColumns = @JoinColumn(name = "encaissement_id"))
    @Builder.Default
    private java.util.List<Encaissement> encaissements = new java.util.ArrayList<>();
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annule_par_id")
    private Utilisateur annulePar;
    @Column(name = "mode_paiement", length = 20)
    private String modePaiement;
    /** TRUE si importé via reprise historique */
    @Column(name = "reprise_historique")
    @Builder.Default
    private boolean repriseHistorique = false;
}
