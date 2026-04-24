package com.ossanasur.cbconnect.module.finance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnnulerPaiementRequest(

        @NotBlank(message = "Le motif d'annulation est obligatoire") @Size(max = 500, message = "Le motif d'annulation ne doit pas dépasser 1000 caractères") String motifAnnulation

) {
}
