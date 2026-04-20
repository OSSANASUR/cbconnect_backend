package com.ossanasur.cbconnect.module.auth.mapper;

import com.ossanasur.cbconnect.module.auth.dto.response.ModuleResponse;
import com.ossanasur.cbconnect.module.auth.entity.ModuleEntity;
import org.springframework.stereotype.Component;

@Component
public class ModuleMapper {
    public ModuleResponse toResponse(ModuleEntity m) {
        if (m == null) return null;
        return new ModuleResponse(
            m.getModuleTrackingId(),
            m.getNomModule(),
            m.getDescription(),
            m.isActif()
        );
    }
}
