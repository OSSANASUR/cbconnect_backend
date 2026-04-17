package com.ossanasur.cbconnect.module.attestation.dto.response;
import com.ossanasur.cbconnect.common.enums.StatutCommandeAttestation;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.UUID;
public record CommandeAttestationResponse(
    UUID commandeTrackingId, String numeroCommande, StatutCommandeAttestation statut,
    Integer quantite, BigDecimal prixUnitaireVente, BigDecimal montantAttestation,
    BigDecimal montantContributionFonds, BigDecimal montantTotal, String montantEnLettres,
    LocalDate dateCommande, LocalDate dateLivraisonEffective, String nomBeneficiaireCheque,
    String organismeRaisonSociale
) {}
