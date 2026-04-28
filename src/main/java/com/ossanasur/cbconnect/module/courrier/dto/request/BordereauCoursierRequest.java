package com.ossanasur.cbconnect.module.courrier.dto.request;

import com.ossanasur.cbconnect.common.enums.TransporteurCourrier;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Payload de création / mise à jour d'un bordereau.
 * Le destinataire est soit un organisme (bureau homologue) soit libre (avocat…).
 *
 * `courriersTrackingIds` = liste ordonnée des courriers à embarquer sur le bordereau.
 */
public record BordereauCoursierRequest(
    UUID destinataireOrganismeTrackingId,
    @Size(max = 255) String destinataireLibre,
    @Size(max = 100) String lieuDepart,
    UUID coursierUtilisateurTrackingId,
    @NotNull TransporteurCourrier transporteur,
    @Size(max = 100) String nomCompagnieBus,
    @Size(max = 80)  String referenceTransporteur,
    BigDecimal montantTransporteur,
    String observations,
    List<UUID> courriersTrackingIds
) {}
