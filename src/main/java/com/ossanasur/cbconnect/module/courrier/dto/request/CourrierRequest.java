package com.ossanasur.cbconnect.module.courrier.dto.request;

import com.ossanasur.cbconnect.common.enums.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CourrierRequest(
    String referenceCourrier,
    @NotNull TypeCourrier typeCourrier,
    @NotNull NatureCourrier nature,
    @NotBlank String expediteur,
    @NotBlank String destinataire,
    @NotBlank String objet,
    @NotNull LocalDate dateCourrier,
    LocalDate dateReception,
    CanalCourrier canal,
    String referenceBordereau,
    UUID sinistreTrackingId,

    /** Destinataire structuré (bureau homologue) — optionnel si destinataire texte libre. */
    UUID destinataireOrganismeTrackingId,

    /** N° sinistre côté homologue (colonne VOTRE REF du bordereau). */
    String numeroSinistreHomologueRef,

    /** Service destinataire interne BNCB (saisi par la secrétaire sur courrier ENTRANT). */
    String serviceDestinataireInterne,

    /** Dispatch multi-destinataires internes (secrétaire → rédacteurs/services). */
    List<DestinataireInterneRequest> destinatairesInternes,

    /** Rattachement direct à un registre journalier (sinon auto-rattachement à OUVERT du jour). */
    UUID registreJourTrackingId
) {}
