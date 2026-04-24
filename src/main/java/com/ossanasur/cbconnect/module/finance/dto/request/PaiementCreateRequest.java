package com.ossanasur.cbconnect.module.finance.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PaiementCreateRequest(

        @NotNull(message = "L'identifiant du sinistre est obligatoire") UUID sinistreTrackingId,

        @NotBlank(message = "Le libellé du bénéficiaire est obligatoire") @Size(max = 255, message = "Le libellé du bénéficiaire ne doit pas dépasser 255 caractères") String beneficiaire,

        UUID beneficiaireVictimeTrackingId,

        UUID beneficiaireOrganismeTrackingId,

        @NotBlank(message = "Le numéro de chèque est obligatoire") @Size(max = 100, message = "Le numéro de chèque ne doit pas dépasser 100 caractères") String numeroChequeEmis,

        @Size(max = 150, message = "Le nom de la banque ne doit pas dépasser 150 caractères") String banqueCheque,

        @NotNull(message = "Le montant est obligatoire") @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à 0") @Digits(integer = 15, fraction = 2, message = "Format montant invalide (max 15 entiers, 2 décimales)") BigDecimal montant,

        @NotNull(message = "La date d'émission est obligatoire") @PastOrPresent(message = "La date d'émission ne peut pas être dans le futur") LocalDate dateEmission,

        @Size(max = 20, message = "Le mode de paiement ne doit pas dépasser 20 caractères") String modePaiement

) {

    /**
     * Contrainte XOR : exactement un des deux identifiants bénéficiaire
     * doit être fourni.
     *
     * <p>
     * Vrai ↔ {@code (victimeId == null) XOR (organismeId == null)}.
     */
    @AssertTrue(message = "Exactement un bénéficiaire doit être renseigné : victime OU organisme, pas les deux")
    public boolean isBeneficiaireXOR() {
        boolean hasVictime = beneficiaireVictimeTrackingId != null;
        boolean hasOrganisme = beneficiaireOrganismeTrackingId != null;
        return hasVictime ^ hasOrganisme;
    }
}
