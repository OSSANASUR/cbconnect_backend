package com.ossanasur.cbconnect.module.auth.dto.request;

import com.ossanasur.cbconnect.common.enums.TypeOrganisme;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record OrganismeRequest(
        @NotNull TypeOrganisme typeOrganisme,
        @NotBlank String raisonSociale,
        @NotBlank String code,
        @Email @NotBlank String email,
        String responsable,
        List<String> contacts,
        String codePays,
        String codePaysBCB,
        Integer paysId,
        String adresse,
        String boitePostale,
        String ville,
        LocalDate dateCreation,
        String numeroAgrement,
        String apiEndpointUrl,
        boolean active,
        String telephonePrincipal,
        String fax,
        String siteWeb,
        // Branding (V2026042601)
        String logo,
        String headerImageUrl,
        String footerImageUrl,
        String titreResponsable,
        boolean afficherDeuxSignatures,
        String responsable2,
        String titreResponsable2) {

    @AssertTrue(message = "Si afficherDeuxSignatures est vrai, responsable2 et titreResponsable2 sont obligatoires")
    public boolean isDeuxSignaturesCoherentes() {
        if (!afficherDeuxSignatures) return true;
        return responsable2 != null && !responsable2.isBlank()
            && titreResponsable2 != null && !titreResponsable2.isBlank();
    }
}
