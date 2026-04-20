package com.ossanasur.cbconnect.module.messagerie.dto.request;

import java.util.Map;
import java.util.UUID;

public record PreviewTemplateRequest(
        UUID templateTrackingId,
        UUID sinistreTrackingId, // pour substitution auto
        Map<String, String> variablesSupp // variables supplémentaires
) {
}
