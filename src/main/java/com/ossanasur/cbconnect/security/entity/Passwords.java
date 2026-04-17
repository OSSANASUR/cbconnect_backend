package com.ossanasur.cbconnect.security.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.io.Serializable;
import java.util.UUID;

@AllArgsConstructor @NoArgsConstructor @Getter @Setter @SuperBuilder
@Entity @DiscriminatorValue("PASSWORDS")
public class Passwords extends InternalHistorique implements Serializable {
    @Column(name = "passwords_tracking_id", unique = true) private UUID passwordsTrackingId;
    @Column(nullable = false) private String password;
    private boolean isTemporary;
    @ManyToOne @JoinColumn(name = "utilisateur_id", nullable = false) private Utilisateur utilisateur;
}
