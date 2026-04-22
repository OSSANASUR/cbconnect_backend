package com.ossanasur.cbconnect.module.sinistre.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Action sur la position RC d'un sinistre.
 * Les champs requis dépendent de {@code action} :
 *   PROPOSER  → pourcentage obligatoire (0-100)
 *   REJETER   → motifRejet obligatoire (>= 10 caractères)
 *   ACCEPTER  → aucun champ supplémentaire
 *   TRANCHER  → pourcentage obligatoire (0-100)
 */
public record ActionRcRequest(
        @NotNull Action action,
        @Min(0) @Max(100) Integer pourcentage,
        @Size(min = 10) String motifRejet
) {
    public enum Action { PROPOSER, REJETER, ACCEPTER, TRANCHER }
}
