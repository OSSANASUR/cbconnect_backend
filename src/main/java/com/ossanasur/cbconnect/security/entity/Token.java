package com.ossanasur.cbconnect.security.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@DiscriminatorValue("TOKEN")
public class Token extends InternalHistorique {
    @Column(name = "token_tracking_id", unique = true)
    private UUID tokenTrackingId;
    @Column(length = 2000)
    private String accessToken;
    @Column(length = 2000)
    private String refreshToken;
    private boolean mobileToken;
    private boolean isValid;
    @Column(name = "t_expire_at")
    private LocalDateTime expireAt;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private Utilisateur user;
}
