package com.ossanasur.cbconnect.module.sinistre.dto.request;
import com.ossanasur.cbconnect.common.enums.*;
import jakarta.validation.constraints.NotBlank; import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.UUID;
public record VictimeRequest(
    @NotBlank String nom, @NotBlank String prenoms, @NotNull LocalDate dateNaissance,
    @NotBlank String sexe, String nationalite,
    @NotNull StatutActivite statutActivite, BigDecimal revenuMensuel,
    UUID paysResidenceTrackingId, UUID sinistreTrackingId
) {}
