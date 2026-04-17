package com.ossanasur.cbconnect.module.sinistre.dto.response;
import com.ossanasur.cbconnect.common.enums.*;
import java.math.BigDecimal; import java.time.LocalDate; import java.time.LocalDateTime; import java.util.UUID;
public record SinistreResponse(
    UUID sinistreTrackingId, String numeroSinistreLocal, String numeroSinistreHomologue,
    TypeSinistre typeSinistre, StatutSinistre statut, TypeDommage typeDommage,
    LocalDate dateAccident, LocalDate dateDeclaration, String lieuAccident, boolean agglomeration,
    BigDecimal tauxRc, PositionRc positionRc, boolean estPrefinance, boolean estContentieux,
    String paysGestionnaireLibelle, String paysEmetteurLibelle,
    String organismeMembreRaisonSociale, String organismeHomologueRaisonSociale,
    String assureNomComplet, String assureImmatriculation,
    String redacteurNomPrenom, LocalDateTime createdAt
) {}
