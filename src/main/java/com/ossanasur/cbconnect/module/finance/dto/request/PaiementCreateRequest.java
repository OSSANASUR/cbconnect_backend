package com.ossanasur.cbconnect.module.finance.dto.request;

import com.ossanasur.cbconnect.common.enums.TypePrejudice;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public record PaiementCreateRequest(

        @NotNull(message = "L'identifiant du sinistre est obligatoire") UUID sinistreTrackingId,

        @NotBlank(message = "Le libellé du bénéficiaire est obligatoire") @Size(max = 255) String beneficiaire,

        UUID beneficiaireVictimeTrackingId,
        UUID beneficiaireOrganismeTrackingId,

        @NotNull(message = "Le montant est obligatoire") @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à 0") @Digits(integer = 15, fraction = 2) BigDecimal montant,

        @NotNull(message = "Le type de préjudice est obligatoire") TypePrejudice typePrejudice,

        @Size(max = 255, message = "Le motif complémentaire ne peut pas dépasser 255 caractères") String motifComplement

) {
    @AssertTrue(message = "Exactement un bénéficiaire doit être renseigné : victime OU organisme, pas les deux")
    public boolean isBeneficiaireXOR() {
        return (beneficiaireVictimeTrackingId != null) ^ (beneficiaireOrganismeTrackingId != null);
    }
}
