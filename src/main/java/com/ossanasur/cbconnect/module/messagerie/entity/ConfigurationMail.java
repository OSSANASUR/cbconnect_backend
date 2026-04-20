package com.ossanasur.cbconnect.module.messagerie.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.SecuriteMailProtocole;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@DiscriminatorValue("CONFIGURATION_MAIL")
@Table(name = "configuration_mail")
public class ConfigurationMail extends InternalHistorique {

    @Column(name = "tracking_id", unique = true)
    private UUID trackingId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false, unique = true)
    private Utilisateur utilisateur;

    // ── Identité expéditeur ───────────────────────────────────────
    @Column(name = "email_expediteur", nullable = false, length = 200)
    private String emailExpediteur;

    @Column(name = "nom_affiche", length = 200)
    private String nomAffiche;

    // ── SMTP ──────────────────────────────────────────────────────
    @Column(name = "smtp_host", length = 200)
    private String smtpHost;

    @Column(name = "smtp_port")
    private Integer smtpPort;

    @Enumerated(EnumType.STRING)
    @Column(name = "smtp_securite", length = 20)
    private SecuriteMailProtocole smtpSecurite;

    @Column(name = "smtp_username", length = 200)
    private String smtpUsername;

    /** Mot de passe chiffré AES-256 — jamais exposé en clair via API */
    @Column(name = "smtp_password_enc", columnDefinition = "TEXT")
    private String smtpPasswordEnc;

    @Column(name = "smtp_auth")
    @Builder.Default
    private boolean smtpAuth = true;

    // ── IMAP ──────────────────────────────────────────────────────
    @Column(name = "imap_host", length = 200)
    private String imapHost;

    @Column(name = "imap_port")
    private Integer imapPort;

    @Enumerated(EnumType.STRING)
    @Column(name = "imap_securite", length = 20)
    private SecuriteMailProtocole imapSecurite;

    // ── État ──────────────────────────────────────────────────────
    @Column(name = "est_configuree", nullable = false)
    @Builder.Default
    private boolean estConfiguree = false;

    @Column(name = "derniere_synchro")
    private LocalDateTime derniereSynchro;

    @Column(name = "nb_messages_non_lus", nullable = false)
    @Builder.Default
    private int nbMessagesNonLus = 0;

    // ── Signature ─────────────────────────────────────────────────
    @Column(name = "signature", columnDefinition = "TEXT")
    private String signature;
}
