package com.ossanasur.cbconnect.module.attestation.dto.response;
import java.time.LocalDate; import java.util.UUID;
public record TrancheLivraisonResponse(
    UUID trancheTrackingId, String numeroDebutSerie, String numeroFinSerie,
    Integer quantiteLivree, LocalDate dateLivraison,
    UUID commandeTrackingId, String numeroCommande,
    UUID lotTrackingId, String referenceLot
) {}
