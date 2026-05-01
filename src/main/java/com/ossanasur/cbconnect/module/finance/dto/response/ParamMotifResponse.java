package com.ossanasur.cbconnect.module.finance.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.ossanasur.cbconnect.common.enums.TypeMotif;

public record ParamMotifResponse(
        UUID trackingId,
        String libelle,
        TypeMotif type,
        boolean actif,
        LocalDateTime createdAt) {
}
