package com.ossanasur.cbconnect.security.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(@NotBlank String ancienMotDePasse, @NotBlank String nouveauMotDePasse) {
}
