package com.ossanasur.cbconnect.module.attestation.dto.response;
import com.ossanasur.cbconnect.common.enums.StatutCheque;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.UUID;
public record ChequeResponse(
    UUID chequeTrackingId, String numeroCheque, BigDecimal montant, String banqueEmettrice,
    LocalDate dateEmission, LocalDate dateReception, LocalDate dateEncaissement,
    StatutCheque statut, String motifAnnulation,
    UUID factureTrackingId, String numeroFacture,
    UUID commandeTrackingId, String numeroCommande, String organismeRaisonSociale
) {}
