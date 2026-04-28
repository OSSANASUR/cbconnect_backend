package com.ossanasur.cbconnect.module.baremes.dto.request;
import jakarta.validation.constraints.*;

public record BaremePretiumDolorisRequest(
        @NotBlank String qualification,
        @NotNull @Min(0) @Max(1000) Integer points,
        Boolean moral,
        Boolean actif) {
}
