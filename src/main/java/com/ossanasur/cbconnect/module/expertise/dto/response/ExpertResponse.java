package com.ossanasur.cbconnect.module.expertise.dto.response;

import com.ossanasur.cbconnect.common.enums.TauxRetenue;
import com.ossanasur.cbconnect.common.enums.TypeExpert;
import java.math.BigDecimal;
import java.util.UUID;

public record ExpertResponse(
        UUID expertTrackingId,
        TypeExpert typeExpert,
        String nomComplet,
        String specialite,
        String nif,
        String email, // NOUVEAU
        String telephone, // NOUVEAU
        TauxRetenue tauxRetenue,
        BigDecimal montExpertise,
        boolean actif,
        String paysLibelle) {
}