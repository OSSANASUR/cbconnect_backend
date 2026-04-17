package com.ossanasur.cbconnect.module.expertise.dto.request;
import com.ossanasur.cbconnect.common.enums.TypeExpert;
import jakarta.validation.constraints.NotBlank; import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal; import java.util.UUID;
public record ExpertRequest(
    @NotNull TypeExpert typeExpert, @NotBlank String nomComplet,
    String specialite, String nif, BigDecimal tauxRetenue,
    boolean actif, UUID paysTrackingId
) {}
