package com.ossanasur.cbconnect.module.auth.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record BanqueResponse(
    UUID banqueTrackingId,
    String nom,
    String code,
    String codeBic,
    String agence,
    String ville,
    String codePays,
    String telephone,
    LocalDateTime createdAt
) {}
