package com.ossanasur.cbconnect.module.expertise.mapper;

import com.ossanasur.cbconnect.module.expertise.dto.response.ExpertResponse;
import com.ossanasur.cbconnect.module.expertise.entity.Expert;
import org.springframework.stereotype.Component;

@Component
public class ExpertMapper {

    public ExpertResponse toResponse(Expert e) {
        if (e == null)
            return null;
        return new ExpertResponse(
                e.getExpertTrackingId(),
                e.getTypeExpert(),
                e.getNomComplet(),
                e.getSpecialite(),
                e.getNif(),
                e.getEmail(), // nouveau
                e.getTelephone(), // nouveau
                e.getTauxRetenue(),
                e.getMontExpertise(),
                e.isActif(),
                e.getPays() != null ? e.getPays().getLibelle() : null);
    }
}