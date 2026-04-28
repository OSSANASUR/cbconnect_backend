package com.ossanasur.cbconnect.module.baremes.dto.response;
import java.math.BigDecimal;

public record BaremeCapitalisationResponse(
        Integer id, String typeBareme, Integer age,
        BigDecimal prixFrancRente, BigDecimal tauxCapitalisation,
        String tableMortalite, Integer ageLimitePaiement, boolean actif) {
}
