package com.ossanasur.cbconnect.module.sinistre.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.module.auth.entity.Organisme;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.UUID;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@DiscriminatorValue("ASSURE")
public class Assure extends InternalHistorique {
    @Column(name = "assure_tracking_id", unique = true)
    private UUID assureTrackingId;
    @Column(nullable = false)
    private String nomAssure;
    private String prenomAssure;
    @Column(nullable = false)
    private String nomComplet;
    private String numeroPolice;
    private String numeroAttestation;
    @Column(name = "numero_c_grise")
    private String numeroCGrise;
    private String proprietaireVehicule;
    private String immatriculation;
    private String marqueVehicule;
    private String telephone;
    private String adresse;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisme_id")
    private Organisme organisme;
    /**
     * Distingue une personne morale (société, entreprise, association)
     * d'une personne physique.
     * false (défaut) = personne physique
     * true = personne morale
     */
    @Column(name = "est_personne_morale")
    @Builder.Default
    private boolean estPersonneMorale = false;

    /* ── Extension wizard V22 ── */
    private String profession;

    @Column(name = "prochaine_vt")
    private java.time.LocalDate prochaineVT;

    @Column(name = "capacite_vehicule")
    private Integer capaciteVehicule;

    @Column(name = "nb_personnes_a_bord")
    private Integer nbPersonnesABord;

    @Column(name = "a_remorque")
    @Builder.Default
    private boolean aRemorque = false;
}
