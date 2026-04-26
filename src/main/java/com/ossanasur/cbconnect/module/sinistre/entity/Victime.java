package com.ossanasur.cbconnect.module.sinistre.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.*;
import com.ossanasur.cbconnect.module.pays.entity.Pays;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@DiscriminatorValue("VICTIME")
public class Victime extends InternalHistorique {

    @Column(name = "victime_tracking_id", unique = true)
    private UUID victimeTrackingId;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenoms;

    @Column(nullable = false)
    private LocalDate dateNaissance;

    @Column(nullable = false, length = 1)
    private String sexe;

    private String nationalite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TypeVictime typeVictime = TypeVictime.NEUTRE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutVictime statutVictime = StatutVictime.NEUTRE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutActivite statutActivite;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal revenuMensuel = BigDecimal.ZERO;

    @Builder.Default
    private boolean estDcdSuiteBlessures = false;
    private LocalDate dateDeces;

    @Builder.Default
    private boolean lienDecesAccident = false;

    @Column(name = "ossan_ged_correspondent_id")
    private Integer ossanGedCorrespondentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sinistre_id", nullable = false)
    private Sinistre sinistre;

    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "pays_residence_id")
    private Pays paysResidence;

    /* ═══════════════ V22 — Extension adversaire & détail ═══════════════ */
    @Builder.Default
    @Column(name = "est_adversaire")
    private boolean estAdversaire = false;
    private String profession;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_dommage")
    private TypeDommage typeDommage;
    private String telephone;

    @Column(name = "numero_permis")
    private String numeroPermis;

    @Column(name = "categories_permis")
    private String categoriesPermis;

    @Column(name = "date_delivrance")
    private LocalDate dateDelivrance;

    @Column(name = "lieu_delivrance")
    private String lieuDelivrance;

    @Column(name = "marque_vehicule")
    private String marqueVehicule;

    @Column(name = "modele_vehicule")
    private String modeleVehicule;

    @Column(name = "genre_vehicule", length = 30)
    private String genreVehicule;

    @Column(name = "couleur_vehicule", length = 50)
    private String couleurVehicule;

    private String immatriculation;

    @Column(name = "numero_chassis", length = 50)
    private String numeroChassis;

    @Column(name = "prochaine_vt")
    private LocalDate prochaineVT;

    @Column(name = "capacite_vehicule")
    private Integer capaciteVehicule;

    @Column(name = "nb_personnes_a_bord")
    private Integer nbPersonnesABord;

    @Column(name = "proprietaire_vehicule")
    private String proprietaireVehicule;

    @Builder.Default
    @Column(name = "a_remorque")
    private boolean aRemorque = false;

    @Column(name = "assureur_adverse")
    private String assureurAdverse;

    @Column(name = "description_degats", columnDefinition = "TEXT")
    private String descriptionDegats;

    @Builder.Default
    @Column(name = "blesses_legers")
    private Integer blessesLegers = 0;

    @Builder.Default
    @Column(name = "blesses_graves")
    private Integer blessesGraves = 0;

    @Builder.Default
    @Column(name = "deces")
    private Integer deces = 0;

    /*
     * ═══════════════ V27 — Négociation RC par adversaire ═══════════════
     * Ces champs ne sont pertinents que quand estAdversaire = true.
     * Chaque adversaire a sa propre négociation avec sa compagnie.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "position_rc", nullable = false)
    @Builder.Default
    private PositionRc positionRc = PositionRc.EN_ATTENTE;

    @Column(name = "pourcentage_rc_propose")
    private Integer pourcentageRcPropose;

    @Column(name = "motif_rejet_rc", columnDefinition = "TEXT")
    private String motifRejetRc;

    @Builder.Default
    @Column(name = "nombre_tours_rc", nullable = false)
    private Integer nombreToursRc = 0;

    @Column(name = "pourcentage_rc_final")
    private Integer pourcentageRcFinal;

    @Column(name = "date_derniere_action_rc")
    private LocalDateTime dateDerniereActionRc;
}
