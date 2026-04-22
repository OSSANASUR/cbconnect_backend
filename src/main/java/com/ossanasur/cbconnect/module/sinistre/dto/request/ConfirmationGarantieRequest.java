package com.ossanasur.cbconnect.module.sinistre.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Confirmation de garantie après déclaration.
 * - garantieAcquise=true  → ATTESTATION DE DECLARATION D'ACCIDENT (statut GARANTIE_CONFIRMEE)
 * - garantieAcquise=false → ATTESTATION DE NON GARANTIE (statut GARANTIE_NON_ACQUISE)
 */
public record ConfirmationGarantieRequest(
    @NotNull Boolean garantieAcquise,
    String referenceGarantie,
    LocalDate dateConfirmationGarantie,
    String observationsGarantie,
    String courrierNonGarantieRef,
    LocalDate courrierNonGarantieDate
) {}
