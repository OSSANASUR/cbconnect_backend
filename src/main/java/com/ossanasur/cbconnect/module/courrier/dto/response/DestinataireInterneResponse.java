package com.ossanasur.cbconnect.module.courrier.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record DestinataireInterneResponse(
    Integer id,
    UUID utilisateurTrackingId,
    String utilisateurNomComplet,
    String serviceLibelle,
    LocalDateTime dateRemiseInterne,
    String observations
) {}
