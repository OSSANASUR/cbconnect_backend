package com.ossanasur.cbconnect.module.finance.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CouvertureSinistreResponse(
        UUID sinistreTrackingId,
        BigDecimal totalEncaisseActif,
        BigDecimal totalPrefiActif,
        BigDecimal totalPaiementsActifs,
        BigDecimal soldeNet,
        boolean regleAOk,
        boolean regleBOk,
        boolean regleCOk,
        String message,
        List<EncaissementResumeInfo> encaissements,
        List<PrefiResumeInfo> prefis) {

    public record EncaissementResumeInfo(
            UUID encaissementTrackingId,
            String numeroCheque,
            BigDecimal montantCheque,
            BigDecimal resteDisponible) {}

    public record PrefiResumeInfo(
            UUID prefinancementTrackingId,
            String libelle,
            BigDecimal montantPrefinance,
            BigDecimal resteARembourser) {}
}
