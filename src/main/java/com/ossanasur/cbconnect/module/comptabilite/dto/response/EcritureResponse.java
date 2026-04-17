package com.ossanasur.cbconnect.module.comptabilite.dto.response;
import com.ossanasur.cbconnect.common.enums.*;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.List; import java.util.UUID;
public record EcritureResponse(
    UUID ecritureTrackingId, String numeroEcriture, TypeTransactionComptable typeTransaction,
    LocalDate dateEcriture, String libelle, BigDecimal montantTotal,
    StatutEcritureComptable statut, String journalCode,
    String sinistreNumeroLocal, List<LigneEcritureResponse> lignes
) {}
