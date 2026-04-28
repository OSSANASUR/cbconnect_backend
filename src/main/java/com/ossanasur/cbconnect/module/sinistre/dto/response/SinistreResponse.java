package com.ossanasur.cbconnect.module.sinistre.dto.response;

import com.ossanasur.cbconnect.common.enums.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record SinistreResponse(
        UUID sinistreTrackingId, String numeroSinistreLocal, String numeroSinistreHomologue,
        TypeSinistre typeSinistre, StatutSinistre statut, TypeDommage typeDommage,
        LocalDate dateAccident, LocalDate dateDeclaration, String lieuAccident, boolean agglomeration,
        BigDecimal tauxRc, PositionRc positionRc, boolean estPrefinance, boolean estContentieux,
        UUID paysGestionnaireTrackingId, String paysGestionnaireLibelle,
        UUID paysEmetteurTrackingId, String paysEmetteurLibelle,
        UUID organismeMembreTrackingId, String organismeMembreRaisonSociale, String organismeMembreCode,
        UUID organismeHomologueTrackingId, String organismeHomologueRaisonSociale,
        UUID assureTrackingId, String assureNomComplet, String assureImmatriculation,
        UUID redacteurTrackingId, String redacteurNomPrenom,
        LocalDateTime createdAt,

        /* ═══════════════ Extension wizard V22 ═══════════════ */
        LocalTime heureAccident,
        String ville, String commune,
        String provenance, String destination,
        String circonstances,
        boolean pvEtabli,
        UUID entiteConstatTrackingId, String entiteConstatNom,
        LocalDate dateEffet, LocalDate dateEcheance,
        List<AssureurSecondaireInfo> assureursSecondaires,
        boolean conducteurEstAssure,
        String conducteurNom, String conducteurPrenom,
        LocalDate conducteurDateNaissance,
        String conducteurNumeroPermis,
        String conducteurCategoriesPermis,
        LocalDate conducteurDateDelivrance, String conducteurLieuDelivrance,
        String declarantNom, String declarantPrenom,
        String declarantTelephone, String declarantQualite,

        /* V24 — Confirmation de garantie */
        Boolean garantieAcquise,
        String referenceGarantie,
        LocalDate dateConfirmationGarantie,
        String observationsGarantie,
        String courrierNonGarantieRef,
        LocalDate courrierNonGarantieDate,

        // V2026042601 — assureur déclarant + n° sinistre côté assureur étranger
        String assureurDeclarant,
        String numeroSinistreAssureur,
        String numeroPoliceAssureur) {
    public record AssureurSecondaireInfo(UUID organismeTrackingId, String raisonSociale, String code) {
    }
}