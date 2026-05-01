package com.ossanasur.cbconnect.module.auth.dto.response;

import com.ossanasur.cbconnect.common.enums.TypeOrganisme;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record OrganismeResponse(
        UUID organismeTrackingId,
        TypeOrganisme typeOrganisme,
        String raisonSociale,
        String code,
        String email,
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
        boolean twoFactorEnabled,
        LocalDateTime createdAt,
        // Coordonnees (V30)
        String telephonePrincipal,
        String fax, String siteWeb,
        // Branding documents (V2026042601)
        String logo,
        String headerImageUrl,
        String footerImageUrl,
        String titreResponsable,
        boolean afficherDeuxSignatures,
        String responsable2,
        String titreResponsable2) {
}
