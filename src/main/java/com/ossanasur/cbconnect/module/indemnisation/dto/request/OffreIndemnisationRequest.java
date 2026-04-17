package com.ossanasur.cbconnect.module.indemnisation.dto.request;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
public record OffreIndemnisationRequest(@NotNull UUID victimeTrackingId) {}
