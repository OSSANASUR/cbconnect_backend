package com.ossanasur.cbconnect.module.finance.dto.response;

import com.ossanasur.cbconnect.common.enums.StatutCheque;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record RemboursementSuggestionResponse(
        BigDecimal resteARembourser,
        BigDecimal montantSuggere,
        List<EncaissementCandidat> candidats
) {
    public record EncaissementCandidat(
            UUID encaissementTrackingId,
            String numeroCheque,
            BigDecimal montantCheque,
            BigDecimal soldeDispoApresImputations,
            LocalDate dateEncaissement,
            StatutCheque statutCheque
    ) {}
}
