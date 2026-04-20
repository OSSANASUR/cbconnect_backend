package com.ossanasur.cbconnect.module.auth.mapper;

import com.ossanasur.cbconnect.module.auth.dto.response.HabilitationResponse;
import com.ossanasur.cbconnect.security.entity.Habilitation;
import org.springframework.stereotype.Component;

@Component
public class HabilitationMapper {
    public HabilitationResponse toResponse(Habilitation h) {
        if (h == null) return null;
        return new HabilitationResponse(
            h.getHabilitationTrackingId(),
            h.getCodeHabilitation(),
            h.getLibelleHabilitation(),
            h.getDescription(),
            h.getAction(),
            h.getTypeAcces(),
            h.getModuleEntity() != null ? h.getModuleEntity().getNomModule() : null,
            h.getModuleEntity() != null ? h.getModuleEntity().getModuleTrackingId() : null
        );
    }
}
