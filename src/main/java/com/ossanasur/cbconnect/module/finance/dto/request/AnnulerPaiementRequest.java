package com.ossanasur.cbconnect.module.finance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnnulerPaiementRequest(
        @NotBlank(message = "Le motif paramétré est obligatoire") @Size(max = 150) String motif,
        @NotBlank(message = "Le motif d'annulation est obligatoire") @Size(max = 500, message = "Le motif d'annulation ne doit pas dépasser 500 caractères") String motifAnnulation) {
}
