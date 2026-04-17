package com.ossanasur.cbconnect.security.dto.request;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
public record RegisterRequest(
    @NotBlank String nom, @NotBlank String prenoms,
    @Email @NotBlank String email, String username, String telephone,
    @NotBlank String password, UUID profilTrackingId
) {}
