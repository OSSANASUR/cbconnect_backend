package com.ossanasur.cbconnect.module.finance.dto.response;

import com.ossanasur.cbconnect.common.enums.StatutPrefinancement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PrefinancementResponse(
        UUID prefinancementTrackingId,
        String numeroPrefinancement,
        UUID sinistreTrackingId,
        String sinistreReference,
        BigDecimal montantPrefinance,
        BigDecimal montantRembourse,
        BigDecimal resteARembourser,
        LocalDate datePrefinancement,
        LocalDateTime dateValidation,
        StatutPrefinancement statut,
        LocalDateTime createdAt,
        String createdBy
) {}
