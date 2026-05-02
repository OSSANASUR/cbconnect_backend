package com.ossanasur.cbconnect.module.finance.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record EncaissementResteResponse(
        UUID encaissementTrackingId,
        String numeroCheque,
        BigDecimal montantCheque,
        BigDecimal totalImpute,
        BigDecimal resteDisponible,
        List<PaiementImputationResponse> imputations) {
}
