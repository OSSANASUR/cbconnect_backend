package com.ossanasur.cbconnect.module.finance.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.ossanasur.cbconnect.common.enums.StatutPaiement;
import com.ossanasur.cbconnect.common.enums.TypeOperationFinanciere;

public record PaiementResponse(

        UUID paiementTrackingId,
        String numeroPaiement,
        TypeOperationFinanciere typeOperation,
        String parentCodeId,
        UUID sinistreTrackingId,
        String sinistreReference,

        String beneficiaire,

        BigDecimal montant,
        String modePaiement,
        String numeroChequeEmis,
        String banqueCheque,

        LocalDate dateEmission,
        LocalDate datePaiement,

        StatutPaiement statut,
        boolean repriseHistorique,

        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime updatedAt,
        String updatedBy,
        // V2026042601
        java.time.LocalDate dateEmissionCheque,
        com.ossanasur.cbconnect.common.enums.TypePrejudice typePrejudice,
        String motifComplement

) {
}
