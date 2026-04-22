package com.ossanasur.cbconnect.module.expertise.dto.request;

import com.ossanasur.cbconnect.common.enums.TypeExpertise;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record AffectationExpertRequest(
        @NotNull UUID expertTrackingId,
        @NotNull UUID victimeTrackingId,
        @NotNull UUID sinistreTrackingId,
        @NotNull TypeExpertise typeExpertise,
        @NotNull LocalDate dateAffectation,
        LocalDate dateLimiteRapport,
        String observations) {
}