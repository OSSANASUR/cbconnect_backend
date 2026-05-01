package com.ossanasur.cbconnect.module.baremes.dto.request;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record BaremePrejudiceMoral266Request(
        @NotBlank String lienParente,
        @NotNull @DecimalMin("0.0") @DecimalMax("500.0") BigDecimal cle,
        String plafondCategorie,
        Boolean actif) {
}
