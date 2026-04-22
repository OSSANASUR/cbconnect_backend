package com.ossanasur.cbconnect.module.messagerie.dto.request;

import jakarta.validation.constraints.NotBlank;

// ════════════════════════════════════════════════════════════
//  Requête changement mot de passe mail uniquement
// ════════════════════════════════════════════════════════════
public record ChangeMotDePasseMailRequest(
                @NotBlank String nouveauMotDePasse) {
}
