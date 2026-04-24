package com.ossanasur.cbconnect.module.baremes.dto.response;
import java.math.BigDecimal;

public record BaremeCleRepartition265Response(
        Integer id, String codeSituation, String libelleSituation,
        boolean conditionConjoint, boolean conditionEnfant, Integer nombreMaxEnfants,
        BigDecimal cleAscendants, BigDecimal cleConjoints,
        BigDecimal cleEnfants, BigDecimal cleOrphelinsDoubles, boolean actif) {
}
