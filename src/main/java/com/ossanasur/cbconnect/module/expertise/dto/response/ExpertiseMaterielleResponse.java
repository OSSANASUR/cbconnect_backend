package com.ossanasur.cbconnect.module.expertise.dto.response;

import com.ossanasur.cbconnect.common.enums.TypeExpertise;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ExpertiseMaterielleResponse(
        UUID expertiseMaTrackingId,
        TypeExpertise typeExpertise,
        LocalDate dateDemande,
        LocalDate dateRapport,
        // Victime + expert + sinistre
        UUID victimeTrackingId,
        String victimeNomPrenom,
        UUID sinistreTrackingId,
        String numeroSinistreLocal,
        String expertNomComplet,
        // Véhicule
        String marqueVehicule,
        String modeleVehicule,
        String immatriculation,
        Integer anneeVehicule,
        String natureDommages,
        boolean estVei,
        BigDecimal valeurVehiculeNeuf,
        BigDecimal valeurVenal,
        BigDecimal valeurReparable,
        // Montants
        BigDecimal montantDevis,
        BigDecimal montantDitExpert,
        BigDecimal honoraires,
        String observations,
        Integer ossanGedDocumentId,
        boolean rapportRecu) {
}
