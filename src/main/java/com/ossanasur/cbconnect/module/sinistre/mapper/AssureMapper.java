package com.ossanasur.cbconnect.module.sinistre.mapper;

import com.ossanasur.cbconnect.module.sinistre.dto.response.AssureResponse;
import com.ossanasur.cbconnect.module.sinistre.entity.Assure;
import org.springframework.stereotype.Component;

@Component
public class AssureMapper {
    public AssureResponse toResponse(Assure a) {
        if (a == null) return null;
        return new AssureResponse(
                a.getAssureTrackingId(),
                a.getNomAssure(),
                a.getPrenomAssure(),
                a.getNomComplet(),
                a.getNumeroPolice(),
                a.getNumeroAttestation(),
                a.getNumeroCGrise(),
                a.getProprietaireVehicule(),
                a.getImmatriculation(),
                a.getMarqueVehicule(),
                a.getTelephone(),
                a.getAdresse(),
                a.getOrganisme() != null ? a.getOrganisme().getOrganismeTrackingId() : null,
                a.getOrganisme() != null ? a.getOrganisme().getRaisonSociale() : null,
                a.getOrganisme() != null ? a.getOrganisme().getCode() : null,
                a.isEstPersonneMorale(),
                a.getProfession(),
                a.getProchaineVT(),
                a.getCapaciteVehicule(),
                a.getNbPersonnesABord(),
                a.isARemorque()
        );
    }
}
