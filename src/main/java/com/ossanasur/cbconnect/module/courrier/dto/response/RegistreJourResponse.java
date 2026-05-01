package com.ossanasur.cbconnect.module.courrier.dto.response;

import com.ossanasur.cbconnect.common.enums.StatutRegistre;
import com.ossanasur.cbconnect.common.enums.TypeRegistre;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record RegistreJourResponse(
    UUID registreTrackingId,
    LocalDate dateJour,
    TypeRegistre typeRegistre,
    StatutRegistre statut,

    UUID secretaireUtilisateurTrackingId,
    String secretaireNomComplet,

    LocalDateTime dateCloture,
    String closPar,

    UUID viseParUtilisateurTrackingId,
    String viseParNomComplet,
    LocalDateTime dateVisa,
    String commentaireChef,

    Integer scanGedDocumentId,

    int nombreCourriers,

    LocalDateTime createdAt
) {}
