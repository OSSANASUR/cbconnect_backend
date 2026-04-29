package com.ossanasur.cbconnect.module.courrier.dto.response;

import com.ossanasur.cbconnect.common.enums.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CourrierResponse(
    UUID courrierTrackingId,
    String referenceCourrier,
    TypeCourrier typeCourrier,
    NatureCourrier nature,
    String expediteur,
    String destinataire,
    String objet,
    LocalDate dateCourrier,
    LocalDate dateReception,
    CanalCourrier canal,
    String referenceBordereau,
    boolean traite,
    LocalDateTime dateTraitement,
    String sinistreNumeroLocal,
    UUID sinistreTrackingId,

    // Flux physique (bordereau)
    UUID bordereauTrackingId,
    String numeroBordereau,
    Integer ordreDansBordereau,
    String numeroSinistreHomologueRef,
    UUID destinataireOrganismeTrackingId,
    String destinataireOrganismeLibelle,

    // Registre journalier
    UUID registreJourTrackingId,
    String serviceDestinataireInterne,
    List<DestinataireInterneResponse> destinatairesInternes
) {}
