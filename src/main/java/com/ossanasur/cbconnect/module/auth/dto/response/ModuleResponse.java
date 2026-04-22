package com.ossanasur.cbconnect.module.auth.dto.response;

import java.util.UUID;

public record ModuleResponse(
    UUID moduleTrackingId,
    String nomModule,
    String description,
    boolean actif
) {}
