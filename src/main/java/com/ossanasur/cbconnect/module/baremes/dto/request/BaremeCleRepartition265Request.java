package com.ossanasur.cbconnect.module.baremes.dto.request;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record BaremeCleRepartition265Request(
        @NotBlank String codeSituation,
        @NotBlank String libelleSituation,
        Boolean conditionConjoint,
        Boolean conditionEnfant,
        Integer nombreMaxEnfants,
        @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal cleAscendants,
        @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal cleConjoints,
        @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal cleEnfants,
        @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal cleOrphelinsDoubles,
        Boolean actif) {
}
