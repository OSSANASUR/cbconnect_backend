package com.ossanasur.cbconnect.module.sinistre.dto.request;

import com.ossanasur.cbconnect.common.enums.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record SinistreRequest(
        @NotNull TypeSinistre typeSinistre, @NotNull TypeDommage typeDommage,
        @NotNull LocalDate dateAccident, LocalDate dateDeclaration,
        String lieuAccident, boolean agglomeration,
        PositionRc positionRc,
        UUID paysGestionnaireTrackingId, UUID paysEmetteurTrackingId,
        UUID organismeMembreTrackingId, UUID organismeHomologueTrackingId,
        UUID assureTrackingId, UUID redacteurTrackingId,
        String numeroSinistreManuel, String numeroSinistreHomologue, String numeroSinistreEcarteBrune,

        /* ═══════════════ Extension wizard V22 ═══════════════ */
        /* Accident */
        LocalTime heureAccident,
        String ville, String commune,
        String provenance, String destination,
        String circonstances,
        Boolean pvEtabli,
        UUID entiteConstatTrackingId,

        /* Contrat */
        LocalDate dateEffet, LocalDate dateEcheance,
        List<UUID> assureursSecondairesTrackingIds,

        /* Conducteur (flat) */
        Boolean conducteurEstAssure,
        String conducteurNom, String conducteurPrenom,
        LocalDate conducteurDateNaissance,
        String conducteurNumeroPermis,
        List<String> conducteurCategoriesPermis,
        LocalDate conducteurDateDelivrance,
        String conducteurLieuDelivrance,

        /* Déclarant (flat) */
        String declarantNom, String declarantPrenom,
        String declarantTelephone, String declarantQualite
) {
}
