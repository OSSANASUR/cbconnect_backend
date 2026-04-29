package com.ossanasur.cbconnect.module.courrier.dto.request;

/** Visa du registre par le chef (commentaire + scan optionnel du registre signé). */
public record VisaRegistreRequest(
    String commentaireChef,
    Integer scanGedDocumentId
) {}
