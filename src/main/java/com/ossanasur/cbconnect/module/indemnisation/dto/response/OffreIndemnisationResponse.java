package com.ossanasur.cbconnect.module.indemnisation.dto.response;
import java.math.BigDecimal; import java.time.LocalDateTime; import java.util.UUID;
public record OffreIndemnisationResponse(
    UUID offreTrackingId, BigDecimal smigMensuelRetenu,
    BigDecimal montantFraisMedicaux, BigDecimal montantItt,
    BigDecimal montantPrejPhysiologique, BigDecimal montantPrejEconomique,
    BigDecimal montantPrejMoral, BigDecimal montantTiercePersonne,
    BigDecimal montantPretiumDoloris, BigDecimal montantPrejEsthetique,
    BigDecimal montantPrejCarriere, BigDecimal montantPrejScolaire,
    BigDecimal montantPrejLeses, BigDecimal montantFraisFuneraires,
    BigDecimal totalBrut, BigDecimal tauxPartageRc, BigDecimal totalNet,
    BigDecimal fraisGestion, BigDecimal montantTotalOffre,
    LocalDateTime dateValidation, String victimeNomPrenom,
    String validePar, LocalDateTime createdAt
) {}
