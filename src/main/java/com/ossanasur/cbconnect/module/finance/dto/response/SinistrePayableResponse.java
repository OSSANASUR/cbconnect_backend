package com.ossanasur.cbconnect.module.finance.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SinistrePayableResponse(
        UUID sinistreTrackingId,
        String sinistreLibelle,
        String numeroSinistre,
        String numeroPolice,
        LocalDate dateSinistre,
        String typeExpertise,
        LocalDate dateRapport,
        BigDecimal montantHtPropose,
        BigDecimal fondsDisponibles,
        BigDecimal totalEncaisse,
        BigDecimal totalDejaPaye,
        boolean dejaPaye
) {}
