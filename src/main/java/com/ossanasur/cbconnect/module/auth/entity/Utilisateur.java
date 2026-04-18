package com.ossanasur.cbconnect.module.auth.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@DiscriminatorValue("UTILISATEUR")
public class Utilisateur extends InternalHistorique {

    @Column(name = "utilisateur_tracking_id", unique = true)
    private UUID utilisateurTrackingId;

    @Column(nullable = false)
    private String nom;
    @Column(nullable = false)
    private String prenoms;
    @Column(unique = true, nullable = false)
    private String email;
    private String username;
    private String telephone;

    /** Code envoye a l'email lors de l'activation */
    private UUID verificationCode;
    private LocalDateTime verificationCodeGeneratedAt;
    private boolean isActive;
    private boolean mustChangePassword;
    private boolean canConnectToMultipleDevices;

    private String accountSetupToken;
    private LocalDateTime accountSetupTokenExpiresAt;
    private String resetToken;
    private LocalDateTime resetTokenExpiresAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "profil_id")
    private Profil profil;
}
