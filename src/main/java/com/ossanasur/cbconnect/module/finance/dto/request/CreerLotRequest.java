package com.ossanasur.cbconnect.module.finance.dto.request;

import com.ossanasur.cbconnect.common.enums.TauxRetenue;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreerLotRequest(
        @NotNull UUID expertTrackingId,
        @NotNull TauxRetenue tauxRetenue,
        @NotEmpty @Valid List<LigneLotRequest> lignes
) {
    public record LigneLotRequest(
            @NotNull UUID sinistreTrackingId,
            @NotNull @DecimalMin("0.01") @Digits(integer = 15, fraction = 2) BigDecimal montantHt
    ) {}
}
