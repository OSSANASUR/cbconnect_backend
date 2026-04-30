package com.ossanasur.cbconnect.module.expertise.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.TypeExpertise;
import com.ossanasur.cbconnect.module.courrier.entity.Courrier;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import com.ossanasur.cbconnect.module.sinistre.entity.Victime;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Affectation d'un expert à une victime pour un type d'expertise.
 *
 * Règles métier :
 * - Un expert peut être affecté à plusieurs victimes d'un même sinistre.
 * - Une victime peut avoir plusieurs expertises (par TypeExpertise).
 * - Contrainte UNIQUE (expert_id, victime_id, type_expertise).
 * - La création génère automatiquement 2 courriers :
 * * Note de mission → expert
 * * Lettre de prévenance → victime
 * - Si email connu → envoi mail optionnel en plus de l'impression physique.
 */
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@DiscriminatorValue("AFFECTATION_EXPERT")
@Table(name = "affectation_expert", uniqueConstraints = @UniqueConstraint(columnNames = { "expert_id", "victime_id",
        "type_expertise" }))
public class AffectationExpert extends InternalHistorique {

    @Column(name = "affectation_tracking_id", unique = true)
    private UUID affectationTrackingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expert_id", nullable = false)
    private Expert expert;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "victime_id", nullable = false)
    private Victime victime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sinistre_id", nullable = false)
    private Sinistre sinistre;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_expertise", nullable = false, length = 30)
    private TypeExpertise typeExpertise;

    @Column(name = "date_affectation", nullable = false)
    private LocalDate dateAffectation;

    @Column(name = "date_limite_rapport")
    private LocalDate dateLimiteRapport;

    /** EN_ATTENTE → RAPPORT_RECU → CLOTURE */
    @Column(nullable = false, length = 30)
    @Builder.Default
    private String statut = "EN_ATTENTE";

    // Courriers générés
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courrier_mission_id")
    private Courrier courrierMission; // Note de mission → expert

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courrier_victime_id")
    private Courrier courrierVictime; // Lettre de prévenance → victime

    @Column(name = "mail_expert_envoye")
    @Builder.Default
    private boolean mailExpertEnvoye = false;

    @Column(name = "mail_victime_envoye")
    @Builder.Default
    private boolean mailVictimeEnvoye = false;

    private String observations;
}
