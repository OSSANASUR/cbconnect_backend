package com.ossanasur.cbconnect.module.auth.dto.request;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
public record UtilisateurRequest(
    @NotBlank String nom,
    @NotBlank String prenoms,
    @Email @NotBlank String email,
    String username,
    String telephone,
    UUID profilTrackingId
) {}
