package com.ossanasur.cbconnect.module.finance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record EncaissementRequest(
        @NotBlank String numeroCheque, @NotNull BigDecimal montantCheque, @NotNull BigDecimal montantTheorique,
        BigDecimal produitFraisGestion, @NotNull LocalDate dateEmission,
        LocalDate dateReception, String banqueEmettrice,
        @NotNull UUID organismeEmetteurTrackingId, @NotNull UUID sinistreTrackingId) {
}
