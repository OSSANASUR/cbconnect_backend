package com.ossanasur.cbconnect.module.finance.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RembourserPrefinancementRequest(
        @NotNull UUID encaissementSourceTrackingId,
        @NotNull @DecimalMin("0.01") BigDecimal montant,
        @NotNull LocalDate dateRemboursement
) {}
