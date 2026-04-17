package com.ossanasur.cbconnect.security.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.ActionHabilitation;
import com.ossanasur.cbconnect.common.enums.TypeAccesHabilitation;
import com.ossanasur.cbconnect.module.auth.entity.ModuleEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.io.Serializable;
import java.util.UUID;

@AllArgsConstructor @NoArgsConstructor @Getter @Setter @SuperBuilder
@Entity @DiscriminatorValue("HABILITATION")
public class Habilitation extends InternalHistorique implements Serializable {
    @Column(name = "habilitation_tracking_id", unique = true) private UUID habilitationTrackingId;
    @Column(unique = true) private String codeHabilitation;
    private String libelleHabilitation;
    private String description;
    @Enumerated(EnumType.STRING) private ActionHabilitation action;
    @Enumerated(EnumType.STRING) private TypeAccesHabilitation typeAcces;
    @ManyToOne @JoinColumn(name = "module_entity_id") private ModuleEntity moduleEntity;
}
