package com.ossanasur.cbconnect.module.attestation.dto.request;
import jakarta.validation.constraints.Min; import jakarta.validation.constraints.NotBlank; import jakarta.validation.constraints.NotNull;
import java.time.LocalDate; import java.util.UUID;
public record TrancheLivraisonRequest(
    @NotNull UUID lotTrackingId,
    @NotBlank String numeroDebutSerie,
    @NotBlank String numeroFinSerie,
    @NotNull @Min(1) Integer quantiteLivree,
    @NotNull LocalDate dateLivraison
) {}
