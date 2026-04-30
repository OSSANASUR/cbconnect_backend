package com.ossanasur.cbconnect.module.courrier.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.*;
import com.ossanasur.cbconnect.module.auth.entity.Organisme;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.module.messagerie.entity.TemplateMail;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@DiscriminatorValue("COURRIER")
public class Courrier extends InternalHistorique {
    @Column(name = "courrier_tracking_id", unique = true)
    private UUID courrierTrackingId;

    @Column(unique = true, nullable = false)
    private String referenceCourrier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeCourrier typeCourrier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NatureCourrier nature;

    @Column(nullable = false)
    private String expediteur;

    @Column(nullable = false)
    private String destinataire;

    @Column(nullable = false)
    private String objet;

    @Column(nullable = false)
    private LocalDate dateCourrier;

    private LocalDate dateReception;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CanalCourrier canal = CanalCourrier.MAIL;

    private String referenceBordereau;

    @Builder.Default
    private boolean traite = false;

    private LocalDateTime dateTraitement;

    @Column(name = "ossan_ged_document_id")
    private Integer ossanGedDocumentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sinistre_id")
    private Sinistre sinistre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "traite_par_id")
    private Utilisateur traitePar;

    /** Message-ID SMTP — permet le threading des réponses */
    @Column(name = "message_id_mail", length = 500)
    private String messageIdMail;

    /** Corps HTML complet du mail envoyé / reçu */
    @Column(name = "corps_html", columnDefinition = "TEXT")
    private String corpsHtml;

    /** TRUE si ce courrier a été envoyé via la messagerie intégrée CBConnect */
    @Column(name = "envoye_par_mail")
    @Builder.Default
    private boolean envoyeParMail = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_envoi", length = 20)
    private StatutMailEnvoye statutEnvoi;

    @Column(name = "date_envoi")
    private java.time.LocalDateTime dateEnvoi;

    /** Template utilisé pour générer ce courrier (null si libre) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private TemplateMail template;

    // ══════ Flux physique — bordereau coursier ══════════════════════════════

    /** Bordereau de transmission auquel ce courrier est rattaché (SORTANT PHYSIQUE). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bordereau_id")
    private BordereauCoursier bordereau;

    /** Ordre de la ligne dans le bordereau imprimé. */
    @Column(name = "ordre_dans_bordereau")
    private Integer ordreDansBordereau;

    /** N° sinistre côté homologue (colonne « N° SIN VOTRE REF » du bordereau). */
    @Column(name = "numero_sinistre_homologue_ref", length = 60)
    private String numeroSinistreHomologueRef;

    /** Destinataire structuré : bureau homologue (optionnel, sinon `destinataire` libre). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinataire_organisme_id")
    private Organisme destinataireOrganisme;

    // ══════ Registre journalier — secrétaire ═══════════════════════════════

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registre_jour_id")
    private RegistreJour registreJour;

    /** Service destinataire interne BNCB (saisie libre, ex: "Rédaction"). */
    @Column(name = "service_destinataire_interne", length = 100)
    private String serviceDestinataireInterne;

    /** Destinataires internes multiples (dispatch par la secrétaire). */
    @OneToMany(mappedBy = "courrier", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CourrierDestinataireInterne> destinatairesInternes = new ArrayList<>();
}
