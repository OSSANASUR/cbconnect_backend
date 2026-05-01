package com.ossanasur.cbconnect.common.enums;

import java.math.BigDecimal;

public enum TauxRetenue {
    TROIS_POURCENT(new BigDecimal("0.03")),
    CINQ_POURCENT(new BigDecimal("0.05")),
    VINGT_POURCENT(new BigDecimal("0.20"));

    private final BigDecimal valeur;

    TauxRetenue(BigDecimal valeur) {
        this.valeur = valeur;
    }

    public BigDecimal getValeur() {
        return valeur;
    }
}
