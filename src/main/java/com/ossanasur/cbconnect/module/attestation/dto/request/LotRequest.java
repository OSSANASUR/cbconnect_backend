package com.ossanasur.cbconnect.module.attestation.dto.request;
import jakarta.validation.constraints.Min; import jakarta.validation.constraints.NotBlank; import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal; import java.time.LocalDate;
public record LotRequest(
    @NotBlank String referenceLot, @NotBlank String nomFournisseur, String numeroBonCommande,
    @NotNull @Min(1) Integer quantite, @NotBlank String numeroDebutSerie, @NotBlank String numeroFinSerie,
    @NotNull BigDecimal prixUnitaireAchat, @NotNull LocalDate dateCommande, LocalDate dateLivraisonFournisseur
) {}
