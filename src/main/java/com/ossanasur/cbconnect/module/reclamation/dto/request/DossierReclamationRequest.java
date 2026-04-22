package com.ossanasur.cbconnect.module.reclamation.dto.request;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
public record DossierReclamationRequest(
    @NotNull UUID sinistreTrackingId,
    @NotNull UUID victimeTrackingId,
    UUID redacteurTrackingId,
    @Size(max = 1000) String notesRedacteur
) {}
