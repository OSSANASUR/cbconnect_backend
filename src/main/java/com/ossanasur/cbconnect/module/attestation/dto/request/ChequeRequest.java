package com.ossanasur.cbconnect.module.attestation.dto.request;
import jakarta.validation.constraints.NotBlank; import jakarta.validation.constraints.NotNull; import jakarta.validation.constraints.Positive;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.UUID;
public record ChequeRequest(
    @NotNull UUID factureTrackingId,
    @NotBlank String numeroCheque,
    @NotNull @Positive BigDecimal montant,
    String banqueEmettrice,
    @NotNull LocalDate dateEmission,
    LocalDate dateReception
) {}
