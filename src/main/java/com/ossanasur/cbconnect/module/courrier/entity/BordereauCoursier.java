package com.ossanasur.cbconnect.module.courrier.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.StatutBordereau;
import com.ossanasur.cbconnect.common.enums.TransporteurCourrier;
import com.ossanasur.cbconnect.module.auth.entity.Organisme;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Bordereau de transmission de courriers — identique à l'imprimé physique
 * utilisé par le BNCB. Regroupe N courriers partant ensemble vers
 * UN SEUL destinataire (bureau homologue OU avocat/autre).
 *
 * Workflow :
 *   BROUILLON → IMPRIME → REMIS_TRANSPORTEUR → DECHARGE_RECUE
 *                            ↘ RETOURNE (exceptionnel)
 */
@Entity
@Table(name = "bordereau_coursier")
@DiscriminatorValue("BORDEREAU_COURSIER")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BordereauCoursier extends InternalHistorique {

    @Column(name = "bordereau_tracking_id", nullable = false, unique = true)
    private UUID bordereauTrackingId;

    /** Numéro auto : BORD/AAAA/NNNN/BNCB-TG */
    @Column(name = "numero_bordereau", nullable = false, length = 40)
    private String numeroBordereau;

    /** Destinataire = bureau homologue OU destinataire libre (avocat, ministère…). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinataire_organisme_id")
    private Organisme destinataireOrganisme;

    @Column(name = "destinataire_libre")
    private String destinataireLibre;

    @Column(name = "lieu_depart", nullable = false, length = 100)
    @Builder.Default
    private String lieuDepart = "Lomé";

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_remise_coursier")
    private LocalDateTime dateRemiseCoursier;

    @Column(name = "date_remise_transporteur")
    private LocalDateTime dateRemiseTransporteur;

    @Column(name = "date_decharge_recue")
    private LocalDateTime dateDechargeRecue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coursier_id")
    private Utilisateur coursier;

    @Enumerated(EnumType.STRING)
    @Column(name = "transporteur", nullable = false, length = 30)
    private TransporteurCourrier transporteur;

    @Column(name = "nom_compagnie_bus", length = 100)
    private String nomCompagnieBus;

    @Column(name = "reference_transporteur", length = 80)
    private String referenceTransporteur;

    @Column(name = "montant_transporteur", precision = 12, scale = 2)
    private BigDecimal montantTransporteur;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 30)
    @Builder.Default
    private StatutBordereau statut = StatutBordereau.BROUILLON;

    @Column(name = "decharge_ged_document_id")
    private Integer dechargeGedDocumentId;

    @Column(name = "facture_ged_document_id")
    private Integer factureGedDocumentId;

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    /** Courriers embarqués — mappé depuis la FK bordereau_id sur COURRIER. */
    @OneToMany(mappedBy = "bordereau", fetch = FetchType.LAZY)
    @OrderBy("ordreDansBordereau ASC")
    @Builder.Default
    private List<Courrier> courriers = new ArrayList<>();
}
