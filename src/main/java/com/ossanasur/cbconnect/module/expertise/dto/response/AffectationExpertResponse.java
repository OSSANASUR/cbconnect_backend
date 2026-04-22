package com.ossanasur.cbconnect.module.expertise.dto.response;

import com.ossanasur.cbconnect.common.enums.TypeExpertise;
import java.time.LocalDate;
import java.util.UUID;

public record AffectationExpertResponse(
        UUID affectationTrackingId,
        // Expert
        UUID expertTrackingId,
        String expertNomComplet,
        String expertEmail,
        String expertTelephone,
        String expertTypeLabel,
        // Victime
        UUID victimeTrackingId,
        String victimeNomPrenom,
        String victimeEmail, // champ à ajouter sur Victime si absent
        // Sinistre
        UUID sinistreTrackingId,
        String numeroSinistreLocal,
        // Affectation
        TypeExpertise typeExpertise,
        LocalDate dateAffectation,
        LocalDate dateLimiteRapport,
        String statut,
        // Courriers
        UUID courrierMissionTrackingId,
        UUID courrierVictimeTrackingId,
        boolean mailExpertEnvoye,
        boolean mailVictimeEnvoye,
        String observations) {
}