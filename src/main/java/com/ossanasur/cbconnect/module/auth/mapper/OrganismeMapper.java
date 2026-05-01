package com.ossanasur.cbconnect.module.auth.mapper;

import com.ossanasur.cbconnect.module.auth.dto.response.OrganismeResponse;
import com.ossanasur.cbconnect.module.auth.entity.Organisme;
import org.springframework.stereotype.Component;

@Component
public class OrganismeMapper {
    public OrganismeResponse toResponse(Organisme o) {
        if (o == null)
            return null;

        return OrganismeResponse.builder()
                .organismeTrackingId(o.getOrganismeTrackingId())
                .typeOrganisme(o.getTypeOrganisme())
                .raisonSociale(o.getRaisonSociale())
                .code(o.getCode())
                .email(o.getEmail())
                .responsable(o.getResponsable())
                .contacts(o.getContacts())
                .codePays(o.getCodePays())
                .codePaysBCB(o.getCodePaysBCB())
                .paysId(o.getPaysId())
                .adresse(o.getAdresse())
                .boitePostale(o.getBoitePostale())
                .ville(o.getVille())
                .dateCreation(o.getDateCreation())
                .numeroAgrement(o.getNumeroAgrement())
                .apiEndpointUrl(o.getApiEndpointUrl())
                .active(o.isActive())
                .twoFactorEnabled(o.isTwoFactorEnabled())
                .createdAt(o.getCreatedAt())
                .telephonePrincipal(o.getTelephonePrincipal())
                .fax(o.getFax())
                .siteWeb(o.getSiteWeb())
                .logo(o.getLogo())
                .headerImageUrl(o.getHeaderImageUrl())
                .footerImageUrl(o.getFooterImageUrl())
                .titreResponsable(o.getTitreResponsable())
                .afficherDeuxSignatures(o.isAfficherDeuxSignatures())
                .responsable2(o.getResponsable2())
                .titreResponsable2(o.getTitreResponsable2())
                .build();

    }
}
