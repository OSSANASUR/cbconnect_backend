package com.ossanasur.cbconnect.module.finance.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PrefinancementCreateRequest(
        @NotNull UUID sinistreTrackingId,
        @NotNull @DecimalMin("0.01") BigDecimal montantPrefinance,
        @NotNull LocalDate datePrefinancement,
        @NotBlank @Size(max = 500) String motifDemande
) {}
