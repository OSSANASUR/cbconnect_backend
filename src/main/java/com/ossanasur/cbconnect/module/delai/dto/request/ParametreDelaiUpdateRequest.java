package com.ossanasur.cbconnect.module.delai.dto.request;

import java.math.BigDecimal;

public record ParametreDelaiUpdateRequest(
        BigDecimal valeur,
        BigDecimal seuilAlerte1Pct,
        BigDecimal seuilAlerte2Pct,
        BigDecimal tauxPenalitePct,
        Boolean actif
) {}
