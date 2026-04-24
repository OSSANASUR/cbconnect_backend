package com.ossanasur.cbconnect.module.finance.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public record PaiementCreateRequest(

        @NotNull(message = "L'identifiant du sinistre est obligatoire") UUID sinistreTrackingId,

        @NotBlank(message = "Le libellé du bénéficiaire est obligatoire") @Size(max = 255) String beneficiaire,

        UUID beneficiaireVictimeTrackingId,
        UUID beneficiaireOrganismeTrackingId,

        @NotNull(message = "Le montant est obligatoire") @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à 0") @Digits(integer = 15, fraction = 2) BigDecimal montant

) {
    @AssertTrue(message = "Exactement un bénéficiaire doit être renseigné : victime OU organisme, pas les deux")
    public boolean isBeneficiaireXOR() {
        return (beneficiaireVictimeTrackingId != null) ^ (beneficiaireOrganismeTrackingId != null);
    }
}