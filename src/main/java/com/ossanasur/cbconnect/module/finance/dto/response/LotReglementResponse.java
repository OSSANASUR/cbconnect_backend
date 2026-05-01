package com.ossanasur.cbconnect.module.finance.dto.response;

import com.ossanasur.cbconnect.common.enums.StatutLotReglement;
import com.ossanasur.cbconnect.common.enums.TauxRetenue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record LotReglementResponse(
        UUID lotTrackingId,
        String numeroLot,
        UUID expertTrackingId,
        String expertNomComplet,
        TauxRetenue tauxRetenue,
        StatutLotReglement statut,
        int nombreReglements,
        BigDecimal montantTtcTotal,
        BigDecimal montantTvaTotal,
        BigDecimal montantTaxeTotal,
        String numeroChequeGlobal,
        String banqueCheque,
        LocalDate dateEmissionCheque,
        List<PaiementResponse> paiements,
        LocalDateTime createdAt,
        String createdBy
) {}
