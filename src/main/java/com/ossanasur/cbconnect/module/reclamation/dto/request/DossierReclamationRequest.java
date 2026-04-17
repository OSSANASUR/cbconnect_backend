package com.ossanasur.cbconnect.module.reclamation.dto.request;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
public record DossierReclamationRequest(@NotNull UUID sinistreTrackingId, @NotNull UUID victimeTrackingId, UUID redacteurTrackingId) {}
