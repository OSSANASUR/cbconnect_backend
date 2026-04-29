package com.ossanasur.cbconnect.module.courrier.dto.request;

import jakarta.validation.constraints.NotNull;

/** Étape 3 du cycle : décharge signée reçue de l'homologue (scan archivé en GED). */
public record ConfirmerDechargeRequest(
    @NotNull Integer dechargeGedDocumentId,
    String observations
) {}
