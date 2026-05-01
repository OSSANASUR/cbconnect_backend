package com.ossanasur.cbconnect.module.attestation.dto.request;
import jakarta.validation.constraints.Min; import jakarta.validation.constraints.NotBlank; import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal; import java.time.LocalDate;
public record LotRequest(
    String referenceLot, String nomFournisseur, String numeroBonCommande,
    @NotNull @Min(1) Integer quantite, @NotBlank String numeroDebutSerie, @NotBlank String numeroFinSerie,
    BigDecimal prixUnitaireAchat, LocalDate dateCommande, LocalDate dateLivraisonFournisseur
) {}
