package com.ossanasur.cbconnect.module.sinistre.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.*;
import com.ossanasur.cbconnect.module.auth.entity.Organisme;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.module.pays.entity.Pays;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@DiscriminatorValue("SINISTRE")
public class Sinistre extends InternalHistorique {

    @Column(name = "sinistre_tracking_id", unique = true)
    private UUID sinistreTrackingId;

    @Column(unique = true, nullable = false, length = 30)
    private String numeroSinistreLocal;

    private String numeroSinistreManuel;

    private String numeroSinistreHomologue;

    private String numeroSinistreEcarteBrune;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeSinistre typeSinistre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutSinistre statut = StatutSinistre.NOUVEAU;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeDommage typeDommage;

    @Column(nullable = false)
    private LocalDate dateAccident;

    @Column(nullable = false)
    private LocalDate dateDeclaration;

    private String lieuAccident;

    private boolean agglomeration;

    private BigDecimal tauxRc;
    /*
     * Position RC au niveau du sinistre = agrégat calculé depuis les adversaires
     * (V27) : chaque adversaire (victime estAdversaire=true) porte sa propre
     * négociation RC. Ce champ est maintenu synchrone par le service :
     * - TRANCHEE si tous les adversaires sont TRANCHEE
     * - EN_NEGOCIATION / REJETEE / EN_ATTENTE selon le cas le plus avancé
     */
    @Enumerated(EnumType.STRING)
    private PositionRc positionRc;

    @Builder.Default
    private boolean estPrefinance = false;

    @Builder.Default
    private boolean estContentieux = false;

    private String niveauJuridiction;

    private LocalDate dateProchaineAudience;

    @Column(name = "ossan_ged_dossier_id")
    private Long ossanGedDossierId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pays_gestionnaire_id", nullable = false)
    private Pays paysGestionnaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pays_emetteur_id")
    private Pays paysEmetteur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisme_membre_id")
    private Organisme organismeMembre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisme_homologue_id")
    private Organisme organismeHomologue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assure_id", nullable = false)
    private Assure assure;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "redacteur_id")
    private Utilisateur redacteur;

    @Column(name = "reprise_historique")
    @Builder.Default
    private boolean repriseHistorique = false;

    @Column(name = "assureur_declarant", length = 200)
    private String assureurDeclarant;

    @Column(name = "numero_police_assureur", length = 200)
    private String numeroPoliceAssureur;

    /* ═══════════════ Extension wizard V22 ═══════════════ */

    /* Accident — détails supplémentaires */
    @Column(name = "heure_accident")
    private LocalTime heureAccident;

    @Column(length = 150)
    private String ville;

    @Column(length = 150)
    private String commune;

    @Column(length = 200)
    private String provenance;

    @Column(length = 200)
    private String destination;

    @Column(columnDefinition = "text")
    private String circonstances;

    @Column(name = "pv_etabli")
    @Builder.Default
    private boolean pvEtabli = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entite_constat_id")
    private EntiteConstat entiteConstat;

    /* Contrat */
    @Column(name = "date_effet")
    private LocalDate dateEffet;

    @Column(name = "date_echeance")
    private LocalDate dateEcheance;

    /* Conducteur au moment du sinistre (flat) */
    @Column(name = "conducteur_est_assure")
    @Builder.Default
    private boolean conducteurEstAssure = true;

    @Column(name = "conducteur_nom", length = 200)
    private String conducteurNom;

    @Column(name = "conducteur_prenom", length = 200)
    private String conducteurPrenom;

    @Column(name = "conducteur_date_naissance")
    private LocalDate conducteurDateNaissance;

    @Column(name = "conducteur_numero_permis", length = 50)
    private String conducteurNumeroPermis;

    @Column(name = "conducteur_categories_permis", length = 150)
    private String conducteurCategoriesPermis; // CSV : "B,C"

    @Column(name = "conducteur_date_delivrance")
    private LocalDate conducteurDateDelivrance;

    @Column(name = "conducteur_lieu_delivrance", length = 150)
    private String conducteurLieuDelivrance;

    /* Déclarant (flat) */
    @Column(name = "declarant_nom", length = 200)
    private String declarantNom;

    @Column(name = "declarant_prenom", length = 200)
    private String declarantPrenom;

    @Column(name = "declarant_telephone", length = 30)
    private String declarantTelephone;

    @Column(name = "declarant_qualite", length = 30)
    private String declarantQualite;

    /* ═══════ V24 — Confirmation de garantie ═══════ */
    @Column(name = "garantie_acquise")
    private Boolean garantieAcquise;

    @Column(name = "reference_garantie", length = 50)
    private String referenceGarantie;

    @Column(name = "date_confirmation_garantie")
    private LocalDate dateConfirmationGarantie;

    @Column(name = "observations_garantie", columnDefinition = "TEXT")
    private String observationsGarantie;

    @Column(name = "courrier_non_garantie_ref", length = 120)
    private String courrierNonGarantieRef;

    @Column(name = "courrier_non_garantie_date")
    private LocalDate courrierNonGarantieDate;

    /* Assureurs secondaires (remorque, co-assurance) */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "sinistre_assureur_secondaire", joinColumns = @JoinColumn(name = "sinistre_id"), inverseJoinColumns = @JoinColumn(name = "organisme_id"))
    @Builder.Default
    private Set<Organisme> assureursSecondaires = new HashSet<>();
}
