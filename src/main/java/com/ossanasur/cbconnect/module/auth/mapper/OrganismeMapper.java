package com.ossanasur.cbconnect.module.auth.mapper;
import com.ossanasur.cbconnect.module.auth.dto.response.OrganismeResponse;
import com.ossanasur.cbconnect.module.auth.entity.Organisme;
import org.springframework.stereotype.Component;

@Component
public class OrganismeMapper {
    public OrganismeResponse toResponse(Organisme o) {
        if (o == null) return null;
        return new OrganismeResponse(
            o.getOrganismeTrackingId(), o.getTypeOrganisme(), o.getRaisonSociale(),
            o.getCode(), o.getEmail(), o.getResponsable(), o.getContacts(),
            o.getCodePays(), o.getCodePaysBCB(), o.getPaysId(),
            o.getDateCreation(), o.getNumeroAgrement(), o.getApiEndpointUrl(),
            o.isActive(), o.getCreatedAt(),
            o.getAdresse(), o.getBoitePostale(), o.getVille(),
            o.getTelephonePrincipal(), o.getFax(), o.getSiteWeb()
        );
    }
}
