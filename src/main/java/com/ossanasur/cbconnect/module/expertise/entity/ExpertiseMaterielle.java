package com.ossanasur.cbconnect.module.expertise.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.TypeExpertise;
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
@DiscriminatorValue("EXPERTISE_MATERIELLE")
public class ExpertiseMaterielle extends InternalHistorique {

    @Column(name = "expertise_ma_tracking_id", unique = true)
    private UUID expertiseMaTrackingId;

    /**
     * Type d'expertise : INITIALE, CONTRE_EXPERTISE, AMIABLE, TIERCE_EXPERTISE.
     * Une même victime peut avoir plusieurs expertises par type.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type_expertise", nullable = false, length = 30)
    private TypeExpertise typeExpertise;

    @Column(nullable = false)
    private LocalDate dateDemande;

    private LocalDate dateRapport;

    // ── Véhicule ──────────────────────────────────────────────────
    @Column(name = "marque_vehicule", length = 100)
    private String marqueVehicule;

    @Column(name = "modele_vehicule", length = 100)
    private String modeleVehicule;

    @Column(length = 30)
    private String immatriculation;

    @Column(name = "annee_vehicule")
    private Integer anneeVehicule;

    @Column(name = "nature_dommages", columnDefinition = "TEXT")
    private String natureDommages;

    /** VEI — Véhicule Économiquement Irréparable */
    @Column(name = "est_vei")
    @Builder.Default
    private boolean estVei = false;

    @Column(name = "valeur_vehicule_neuf", precision = 15, scale = 2)
    private BigDecimal valeurVehiculeNeuf;

    /** Valeur vénale avant sinistre */
    @Column(name = "valeur_venal", precision = 15, scale = 2)
    private BigDecimal valeurVenal;

    /** Valeur réparable selon expert */
    @Column(name = "valeur_reparable", precision = 15, scale = 2)
    private BigDecimal valeurReparable;

    // ── Montants expertise ─────────────────────────────────────────
    @Column(name = "montant_devis", precision = 15, scale = 2)
    private BigDecimal montantDevis;

    @Column(name = "montant_dit_expert", precision = 15, scale = 2)
    private BigDecimal montantDitExpert;

    @Builder.Default
    @Column(precision = 15, scale = 2)
    private BigDecimal honoraires = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String observations;

    /** ID du rapport PDF dans OssanGED */
    @Column(name = "ossan_ged_document_id")
    private Integer ossanGedDocumentId;

    // ── Relations ─────────────────────────────────────────────────

    /**
     * Victime liée à cette expertise matérielle.
     * L'expertise matérielle est désormais liée à une victime (propriétaire du
     * véhicule)
     * ET au sinistre, comme l'expertise médicale.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "victime_id")
    private Victime victime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sinistre_id", nullable = false)
    private Sinistre sinistre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expert_id")
    private Expert expert;
}