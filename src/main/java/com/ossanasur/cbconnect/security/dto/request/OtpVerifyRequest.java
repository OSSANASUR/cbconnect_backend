package com.ossanasur.cbconnect.security.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record OtpVerifyRequest(
        @NotNull UUID otpTrackingId,
        @NotBlank @Pattern(regexp = "\\d{6}", message = "Le code doit contenir 6 chiffres") String code
) {}
