package com.ossanasur.cbconnect.security.dto.request;
import jakarta.validation.constraints.NotBlank;
public record ResetPasswordRequest(@NotBlank String token, @NotBlank String nouveauPassword) {}
