package com.ossanasur.cbconnect.security.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@DiscriminatorValue("OTP")
public class OTP extends InternalHistorique {
    @Column(name = "otp_tracking_id", unique = true)
    private UUID otpTrackingId;
    @Column(nullable = false, length = 10)
    private String code;
    private LocalDateTime expiresAt;
    private boolean used;
    private String purpose; // LOGIN, RESET_PASSWORD, ACCOUNT_ACTIVATION
    @ManyToOne
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;
}
