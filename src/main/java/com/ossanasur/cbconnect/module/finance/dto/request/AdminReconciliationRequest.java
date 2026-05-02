package com.ossanasur.cbconnect.module.finance.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record AdminReconciliationRequest(
        @NotNull UUID sinistreTrackingId,
        @NotEmpty @Valid List<PaiementImputations> paiementsImputations) {

    public record PaiementImputations(
            @NotNull UUID paiementTrackingId,
            @NotEmpty @Valid List<ImputationRequest> imputations) {}
}
