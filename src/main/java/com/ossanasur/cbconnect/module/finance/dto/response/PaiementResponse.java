package com.ossanasur.cbconnect.module.finance.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.ossanasur.cbconnect.common.enums.StatutPaiement;

public record PaiementResponse(

        UUID paiementTrackingId,
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
        String updatedBy

) {
}
