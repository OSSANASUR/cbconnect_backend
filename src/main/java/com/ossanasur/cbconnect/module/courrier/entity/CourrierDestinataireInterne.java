package com.ossanasur.cbconnect.module.courrier.entity;

import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Destinataire interne d'un courrier reçu (pour dispatch multi-destinataires
 * par la secrétaire).
 *
 * Un courrier peut être adressé à :
 *   - N utilisateurs identifiés (utilisateur_id)
 *   - OU à un service (service_libelle, texte libre — ex: "Rédaction")
 */
@Entity
@Table(name = "courrier_destinataire_interne")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourrierDestinataireInterne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "courrier_id", nullable = false)
    private Courrier courrier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @Column(name = "service_libelle", length = 150)
    private String serviceLibelle;

    @Column(name = "date_remise_interne")
    private LocalDateTime dateRemiseInterne;

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "created_by")
    private String createdBy;
}
