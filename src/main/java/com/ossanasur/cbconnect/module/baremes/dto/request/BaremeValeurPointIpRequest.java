package com.ossanasur.cbconnect.module.baremes.dto.request;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record BaremeValeurPointIpRequest(
        @NotNull @Min(0) @Max(120) Integer ageMin,
        @Min(0) @Max(120) Integer ageMax,
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal ippMin,
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal ippMax,
        @NotNull @Min(0) Integer valeurPoint,
        Boolean actif) {
}
