package com.ossanasur.cbconnect.module.auth.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.TypeParametre;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@DiscriminatorValue("PARAMETRE")
public class Parametre extends InternalHistorique {

    @Column(name = "parametre_tracking_id", unique = true)
    private UUID parametreTrackingId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeParametre typeParametre;

    @Column(unique = true, nullable = false, length = 100)
    private String cle;

    @Column(nullable = false)
    private String valeur;

    private String description;
}
