package com.ossanasur.cbconnect.module.expertise.dto.request;

import com.ossanasur.cbconnect.common.enums.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ExpertiseMedicaleRequest(
        @NotNull TypeExpertise typeExpertise, @NotNull LocalDate dateDemande,
        LocalDate dateRapport, LocalDate dateConsolidation,
        BigDecimal tauxIpp, Integer dureeIttJours, Integer dureeItpJours,
        QualificationPretium pretiumDoloris, QualificationPretium prejudiceEsthetique,
        Boolean necessiteTiercePersonne, BigDecimal honoraires, BigDecimal honorairesContreExpertise,
        Integer ossanGedDocumentId,
        @NotNull UUID victimeTrackingId, UUID expertTrackingId) {
}
