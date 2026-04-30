package com.ossanasur.cbconnect.module.courrier.dto.request;

import java.util.UUID;

/**
 * Un destinataire interne d'un courrier entrant (pour dispatch par la secrétaire).
 * Soit un utilisateur identifié, soit un service libellé.
 */
public record DestinataireInterneRequest(
    UUID utilisateurTrackingId,
    String serviceLibelle,
    String observations
) {}
