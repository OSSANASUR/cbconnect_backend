package com.ossanasur.cbconnect.module.sinistre.dto.request;

import com.ossanasur.cbconnect.common.enums.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record SinistreRequest(
        @NotNull TypeSinistre typeSinistre, @NotNull TypeDommage typeDommage,
        @NotNull LocalDate dateAccident, LocalDate dateDeclaration,
        String lieuAccident, boolean agglomeration,
        UUID paysGestionnaireTrackingId, UUID paysEmetteurTrackingId,
        UUID organismeMembreTrackingId, UUID organismeHomologueTrackingId,
        UUID assureTrackingId, UUID redacteurTrackingId,
        String numeroSinistreManuel, String numeroSinistreHomologue, String numeroSinistreEcarteBrune) {
}
