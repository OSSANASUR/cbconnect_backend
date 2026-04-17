package com.ossanasur.cbconnect.module.auth.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.io.Serializable;
import java.util.UUID;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @SuperBuilder
@Entity @DiscriminatorValue("MODULE")
public class ModuleEntity extends InternalHistorique implements Serializable {
    @Column(name = "module_tracking_id", unique = true) private UUID moduleTrackingId;
    @Column(nullable = false) private String nomModule;
    private String description;
    @Builder.Default
    private boolean actif = true;
}
