package com.ossanasur.cbconnect.module.attestation.dto.request;
import jakarta.validation.constraints.Min; import jakarta.validation.constraints.NotNull;
import java.util.UUID;
public record CommandeAttestationRequest(@NotNull UUID organismeTrackingId, @NotNull @Min(1) Integer quantite, String nomBeneficiaireCheque) {}
