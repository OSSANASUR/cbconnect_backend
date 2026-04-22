package com.ossanasur.cbconnect.module.sinistre.dto.request;

import java.time.LocalDate;

/** Passage d'un dossier en ARBITRAGE (instance arbitrale / commission). */
public record MiseEnArbitrageRequest(
        LocalDate dateSaisineArbitrage,
        String observations
) {}
