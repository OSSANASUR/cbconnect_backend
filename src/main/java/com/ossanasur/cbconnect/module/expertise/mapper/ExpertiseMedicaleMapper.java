package com.ossanasur.cbconnect.module.expertise.mapper;

import com.ossanasur.cbconnect.module.expertise.dto.response.ExpertiseMedicaleResponse;
import com.ossanasur.cbconnect.module.expertise.entity.ExpertiseMedicale;
import org.springframework.stereotype.Component;

@Component
public class ExpertiseMedicaleMapper {
    public ExpertiseMedicaleResponse toResponse(ExpertiseMedicale e) {
        if (e == null)
            return null;
        return new ExpertiseMedicaleResponse(
                e.getExpertiseMedTrackingId(), e.getTypeExpertise(),
                e.getDateDemande(), e.getDateRapport(), e.getDateConsolidation(),
                e.getTauxIpp(), e.getDureeIttJours(), e.getDureeItpJours(), e.getPretiumDoloris(),
                e.getPrejudiceEsthetique(), e.isNecessiteTiercePersonne(), e.getHonoraires(),
                e.getHonorairesContreExpertise(), e.getOssanGedDocumentId(),
                e.getExpert() != null ? e.getExpert().getNomComplet() : null,
                e.getVictime() != null ? e.getVictime().getNom() + " " + e.getVictime().getPrenoms() : null);
    }
}
