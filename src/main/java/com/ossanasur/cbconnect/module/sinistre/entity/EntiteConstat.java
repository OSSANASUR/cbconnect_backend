package com.ossanasur.cbconnect.module.sinistre.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.TypeEntiteConstat;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.UUID;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@DiscriminatorValue("ENTITE_CONSTAT")
public class EntiteConstat extends InternalHistorique {
    @Column(name = "entite_constat_tracking_id", unique = true)
    private UUID entiteConstatTrackingId;
    @Column(nullable = false)
    private String nom;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeEntiteConstat type;
    private String localite;
    private String codePostal;
    @Builder.Default
    private boolean actif = true;
}
