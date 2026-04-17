package com.ossanasur.cbconnect.module.reclamation.dto.response;
import com.ossanasur.cbconnect.common.enums.StatutDossierReclamation;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.UUID;
public record DossierReclamationResponse(
    UUID dossierTrackingId, String numeroDossier, LocalDate dateOuverture, LocalDate dateCloture,
    StatutDossierReclamation statut, BigDecimal montantTotalReclame, BigDecimal montantTotalRetenu,
    String victimeNomPrenom, String sinistreNumeroLocal
) {}
