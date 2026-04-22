package com.ossanasur.cbconnect.security.dto.response;

import java.time.LocalDateTime;

public record ActivationInfoResponse(
        String email,
        String nomComplet,
        LocalDateTime expiresAt,
        String supportEmail
) {}
