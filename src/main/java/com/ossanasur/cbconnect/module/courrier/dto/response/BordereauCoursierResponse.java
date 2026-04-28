package com.ossanasur.cbconnect.module.courrier.dto.response;

import com.ossanasur.cbconnect.common.enums.StatutBordereau;
import com.ossanasur.cbconnect.common.enums.TransporteurCourrier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record BordereauCoursierResponse(
    UUID bordereauTrackingId,
    String numeroBordereau,

    UUID destinataireOrganismeTrackingId,
    String destinataireOrganismeLibelle,
    String destinataireLibre,
    String lieuDepart,

    LocalDateTime dateCreation,
    LocalDateTime dateRemiseCoursier,
    LocalDateTime dateRemiseTransporteur,
    LocalDateTime dateDechargeRecue,

    UUID coursierUtilisateurTrackingId,
    String coursierNomComplet,

    TransporteurCourrier transporteur,
    String nomCompagnieBus,
    String referenceTransporteur,
    BigDecimal montantTransporteur,

    StatutBordereau statut,

    Integer dechargeGedDocumentId,
    Integer factureGedDocumentId,

    String observations,

    int nombreCourriers,
    List<BordereauLigneResponse> lignes,

    String createdBy,
    LocalDateTime createdAt
) {}
