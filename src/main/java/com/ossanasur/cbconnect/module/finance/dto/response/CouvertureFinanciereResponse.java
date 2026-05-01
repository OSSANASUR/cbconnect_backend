package com.ossanasur.cbconnect.module.finance.dto.response;

import java.math.BigDecimal;

public record CouvertureFinanciereResponse(
        BigDecimal totalEncaissementsActifs,
        BigDecimal totalPrefinancementsActifs,
        BigDecimal totalDisponible,
        BigDecimal totalEngageReglements,
        BigDecimal soldeNet,
        boolean regleAOk,
        boolean regleBOk,
        boolean regleCOk,
        String messageBlocant
) {}
