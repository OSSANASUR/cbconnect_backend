package com.ossanasur.cbconnect.module.baremes.dto.response;
import java.math.BigDecimal;

public record BaremePrejudiceMoral266Response(
        Integer id, String lienParente, BigDecimal cle,
        String plafondCategorie, boolean actif) {
}
