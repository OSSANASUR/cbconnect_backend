package com.ossanasur.cbconnect.module.baremes.dto.request;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record BaremeCapitalisationRequest(
        @NotBlank @Pattern(regexp = "^(M100|F100|M25|F25)$", message = "typeBareme doit être M100, F100, M25 ou F25") String typeBareme,
        @NotNull @Min(0) @Max(120) Integer age,
        @NotNull @DecimalMin("0.0") BigDecimal prixFrancRente,
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal tauxCapitalisation,
        @NotBlank String tableMortalite,
        @NotNull @Min(0) @Max(120) Integer ageLimitePaiement,
        Boolean actif) {
}
