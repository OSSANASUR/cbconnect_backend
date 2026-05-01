package com.ossanasur.cbconnect.module.finance.dto.response;

import com.ossanasur.cbconnect.common.enums.StatutCheque;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Builder
public record EncaissementResponse(
                UUID encaissementTrackingId,
                String numeroCheque,
                BigDecimal montantCheque,
                BigDecimal montantTheorique,
                BigDecimal produitFraisGestion,
                LocalDate dateEmission,
                LocalDate dateReception,
                LocalDate dateEncaissement,
                String banqueEmettrice,
                StatutCheque statutCheque,
                String organismeEmetteurRaisonSociale,
                String sinistreNumeroLocal,
                String chequeOrdreOrganismeNom,
                UUID chequeOrdreOrganismeTrackingId) {
}
