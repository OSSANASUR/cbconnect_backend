package com.ossanasur.cbconnect.module.attestation.dto.response;
import com.ossanasur.cbconnect.common.enums.TypeFactureAttestation;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.UUID;
public record FactureAttestationResponse(
    UUID factureTrackingId, String numeroFacture, TypeFactureAttestation typeFacture,
    LocalDate dateFacture, BigDecimal montantAttestation, BigDecimal montantContributionFonds,
    BigDecimal montantTotal, String montantEnLettres, String instructionCheque,
    LocalDate dateEcheance, boolean annulee, String commandeNumero
) {}
