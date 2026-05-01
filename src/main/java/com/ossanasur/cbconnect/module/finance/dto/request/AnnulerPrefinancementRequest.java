package com.ossanasur.cbconnect.module.finance.dto.request;

import jakarta.validation.constraints.*;

public record AnnulerPrefinancementRequest(
        @NotBlank @Size(max = 500) String motifAnnulation) {
}
