package com.ossanasur.cbconnect.module.auth.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.security.entity.Habilitation;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Profil RBAC. Valeurs attendues pour profilNom :
 * SE, CSS, REDACTEUR, COMPTABLE, SECRETAIRE, STAGIAIRE, ADMIN
 */
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @SuperBuilder
@Entity @DiscriminatorValue("PROFIL")
public class Profil extends InternalHistorique implements Serializable {
    @Column(name = "profil_tracking_id", unique = true) private UUID profilTrackingId;
    @Column(nullable = false) private String profilNom;
    private String commentaire;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "profil_habilitations",
        joinColumns = @JoinColumn(name = "profil_id"),
        inverseJoinColumns = @JoinColumn(name = "habilitation_id"))
    private List<Habilitation> habilitations;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisme_id")
    private Organisme organisme;
}
