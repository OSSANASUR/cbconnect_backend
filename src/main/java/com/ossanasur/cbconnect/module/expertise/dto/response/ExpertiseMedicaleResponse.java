package com.ossanasur.cbconnect.module.expertise.dto.response;
import com.ossanasur.cbconnect.common.enums.*;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.UUID;
public record ExpertiseMedicaleResponse(
    UUID expertiseMedTrackingId, TypeExpertise typeExpertise,
    LocalDate dateDemande, LocalDate dateRapport, LocalDate dateConsolidation,
    BigDecimal tauxIpp, Integer dureeIttJours, QualificationPretium pretiumDoloris,
    QualificationPretium prejudiceEsthetique, boolean necessiteTiercePersonne,
    BigDecimal honoraires, String expertNomComplet, String victimeNomPrenom
) {}
