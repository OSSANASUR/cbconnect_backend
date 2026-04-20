package com.ossanasur.cbconnect.module.messagerie.dto.request;

import com.ossanasur.cbconnect.common.enums.NatureCourrier;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record EnvoyerMailRequest(

        /** Destinataire(s) — emails séparés par virgule */
        @NotBlank String destinataire,

        /** CC optionnel */
        String cc,

        /** Sujet final (après substitution des variables) */
        @NotBlank String sujet,

        /** Corps HTML final */
        @NotBlank String corpsHtml,

        // Lien sinistre (optionnel mais recommandé)
        UUID sinistreTrackingId,

        /** Nature BNCB (RELANCE, DEMANDE_PIECES, etc.) pour traçabilité */
        NatureCourrier nature,

        /** Template utilisé (null si message libre) */
        UUID templateTrackingId) {
}
