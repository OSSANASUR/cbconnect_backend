package com.ossanasur.cbconnect.module.reclamation.dto.request;

import com.ossanasur.cbconnect.common.enums.TypeDepenseReclamation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Ajout d'une facture à un dossier de réclamation.
 * Les champs montantRetenu / lienAvecAccidentVerifie / motifRejet sont optionnels
 * et permettent au rédacteur de créer + trancher en une seule requête.
 * dossierTrackingId peut être fourni dans le body (legacy) ou injecté depuis l'URL
 * par le controller.
 */
public record FactureReclamationRequest(
        String numeroFactureOriginal,
        @NotNull TypeDepenseReclamation typeDepense,
        @NotBlank String nomPrestataire,
        @NotNull LocalDate dateFacture,
        @NotNull BigDecimal montantReclame,
        UUID dossierTrackingId,
        BigDecimal montantRetenu,
        Boolean lienAvecAccidentVerifie,
        String motifRejet
) {}
