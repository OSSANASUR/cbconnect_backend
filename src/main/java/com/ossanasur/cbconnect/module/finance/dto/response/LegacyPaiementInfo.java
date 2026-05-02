package com.ossanasur.cbconnect.module.finance.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record LegacyPaiementInfo(
        UUID paiementTrackingId,
        String numeroPaiement,
        String beneficiaire,
        BigDecimal montant,
        LocalDate dateEmission,
        String statut) {
}
