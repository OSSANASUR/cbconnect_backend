package com.ossanasur.cbconnect.module.expertise.dto.request;

import com.ossanasur.cbconnect.common.enums.TypeExpertise;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ExpertiseMaterielleRequest(
        @NotNull TypeExpertise typeExpertise,
        @NotNull LocalDate dateDemande,
        LocalDate dateRapport,
        // Véhicule
        String marqueVehicule,
        String modeleVehicule,
        String immatriculation,
        Integer anneeVehicule,
        String natureDommages,
        Boolean estVei,
        BigDecimal valeurVehiculeNeuf,
        BigDecimal valeurVenal,
        BigDecimal valeurReparable,
        // Montants expertise
        BigDecimal montantDevis,
        BigDecimal montantDitExpert,
        BigDecimal honoraires,
        String observations,
        // Liens
        @NotNull UUID victimeTrackingId,
        @NotNull UUID sinistreTrackingId,
        UUID expertTrackingId,
        Integer ossanGedDocumentId // rapport PDF uploadé dans la GED
) {
}