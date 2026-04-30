package com.ossanasur.cbconnect.module.courrier.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

/** Étape 2 du cycle : dépôt à la poste / bus / DHL. */
public record MarquerRemisTransporteurRequest(
    @NotBlank String referenceTransporteur,
    BigDecimal montantTransporteur,
    String nomCompagnieBus,
    Integer factureGedDocumentId,
    String observations
) {}
