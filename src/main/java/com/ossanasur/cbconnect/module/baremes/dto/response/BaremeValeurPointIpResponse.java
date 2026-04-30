package com.ossanasur.cbconnect.module.baremes.dto.response;
import java.math.BigDecimal;

public record BaremeValeurPointIpResponse(
        Integer id, Integer ageMin, Integer ageMax,
        BigDecimal ippMin, BigDecimal ippMax,
        Integer valeurPoint, boolean actif) {
}
