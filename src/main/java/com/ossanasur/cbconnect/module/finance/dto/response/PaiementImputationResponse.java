package com.ossanasur.cbconnect.module.finance.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaiementImputationResponse(
        UUID imputationTrackingId,
        UUID paiementTrackingId,
        UUID encaissementTrackingId,
        String numeroEncaissement,
        BigDecimal montantImpute,
        UUID imputationOrigineTrackingId,
        LocalDateTime createdAt,
        String createdBy) {
}
