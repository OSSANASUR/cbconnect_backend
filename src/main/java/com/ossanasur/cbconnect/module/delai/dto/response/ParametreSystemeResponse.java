package com.ossanasur.cbconnect.module.delai.dto.response;

import java.math.BigDecimal;

public record ParametreSystemeResponse(
        Integer id,
        String cle,
        String libelle,
        BigDecimal valeurDecimal,
        String description,
        boolean actif
) {}
