package com.ossanasur.cbconnect.module.finance.dto.response;

import com.ossanasur.cbconnect.common.enums.StatutPrefinancement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PrefinancementDetailResponse(
        UUID prefinancementTrackingId,
        String numeroPrefinancement,
        UUID sinistreTrackingId,
        String sinistreReference,
        BigDecimal montantPrefinance,
        BigDecimal montantRembourse,
        BigDecimal resteARembourser,
        LocalDate datePrefinancement,
        LocalDateTime dateValidation,
        StatutPrefinancement statut,
        LocalDateTime createdAt,
        String createdBy,
        String motifDemande,
        String motifAnnulation,
        String validParLogin,
        String annuleParLogin,
        EcritureInfo ecritureComptable,
        List<RemboursementInfo> remboursements
) {
    public record RemboursementInfo(
            UUID remboursementTrackingId,
            UUID encaissementSourceTrackingId,
            String encaissementNumeroCheque,
            BigDecimal montant,
            LocalDate dateRemboursement,
            String valideeParLogin,
            EcritureInfo ecritureComptable) {
    }

    public record EcritureInfo(
            UUID ecritureTrackingId,
            String numeroEcriture,
            String statut,
            BigDecimal montantTotal,
            LocalDate dateEcriture) {
    }
}
