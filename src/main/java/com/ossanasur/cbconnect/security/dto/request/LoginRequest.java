package com.ossanasur.cbconnect.security.dto.request;
import jakarta.validation.constraints.NotBlank;
public record LoginRequest(@NotBlank String login, @NotBlank String password, boolean isMobile) {}
