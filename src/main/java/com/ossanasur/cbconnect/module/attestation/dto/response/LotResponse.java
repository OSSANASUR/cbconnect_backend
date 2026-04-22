package com.ossanasur.cbconnect.module.attestation.dto.response;
import com.ossanasur.cbconnect.common.enums.StatutLot;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.UUID;
public record LotResponse(
    UUID lotTrackingId, String referenceLot, String nomFournisseur, String numeroBonCommande,
    Integer quantite, String numeroDebutSerie, String numeroFinSerie, BigDecimal prixUnitaireAchat,
    LocalDate dateCommande, LocalDate dateLivraisonFournisseur, StatutLot statutLot,
    Integer quantiteDistribuee, Integer quantiteRestante
) {}
