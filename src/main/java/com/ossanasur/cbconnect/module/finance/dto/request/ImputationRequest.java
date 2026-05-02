package com.ossanasur.cbconnect.module.finance.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record ImputationRequest(
        @NotNull UUID encaissementTrackingId,
        @NotNull @Positive BigDecimal montant) {
}
