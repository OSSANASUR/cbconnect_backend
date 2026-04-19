package com.ossanasur.cbconnect.module.reclamation.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Associe un document GED (déjà scanné et indexé dans Paperless)
 * à une pièce attendue d'un dossier de réclamation.
 */
public record AssocierDocumentRequest(

        /** TrackingId du PaperlessDocument en base CBConnect */
        @NotNull(message = "Le document GED est obligatoire") UUID paperlessDocumentTrackingId,

        String notes) {
}