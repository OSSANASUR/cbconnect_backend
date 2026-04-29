package com.ossanasur.cbconnect.module.courrier.mapper;

import com.ossanasur.cbconnect.module.courrier.dto.response.CourrierResponse;
import com.ossanasur.cbconnect.module.courrier.dto.response.DestinataireInterneResponse;
import com.ossanasur.cbconnect.module.courrier.entity.Courrier;
import com.ossanasur.cbconnect.module.courrier.entity.CourrierDestinataireInterne;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class CourrierMapper {

    public CourrierResponse toResponse(Courrier c) {
        if (c == null) return null;

        var bordereau = c.getBordereau();
        var destOrg   = c.getDestinataireOrganisme();
        var registre  = c.getRegistreJour();

        List<DestinataireInterneResponse> destinataires = c.getDestinatairesInternes() == null
            ? Collections.emptyList()
            : c.getDestinatairesInternes().stream().map(this::toDestinataireInterne).toList();

        return new CourrierResponse(
            c.getCourrierTrackingId(), c.getReferenceCourrier(), c.getTypeCourrier(),
            c.getNature(), c.getExpediteur(), c.getDestinataire(), c.getObjet(),
            c.getDateCourrier(), c.getDateReception(), c.getCanal(),
            c.getReferenceBordereau(),
            c.isTraite(), c.getDateTraitement(),
            c.getSinistre() != null ? c.getSinistre().getNumeroSinistreLocal() : null,
            c.getSinistre() != null ? c.getSinistre().getSinistreTrackingId() : null,

            bordereau != null ? bordereau.getBordereauTrackingId() : null,
            bordereau != null ? bordereau.getNumeroBordereau() : null,
            c.getOrdreDansBordereau(),
            c.getNumeroSinistreHomologueRef(),
            destOrg != null ? destOrg.getOrganismeTrackingId() : null,
            destOrg != null ? destOrg.getRaisonSociale() : null,

            registre != null ? registre.getRegistreTrackingId() : null,
            c.getServiceDestinataireInterne(),
            destinataires
        );
    }

    public DestinataireInterneResponse toDestinataireInterne(CourrierDestinataireInterne d) {
        if (d == null) return null;
        var u = d.getUtilisateur();
        String nomComplet = u != null
            ? ((u.getPrenoms() != null ? u.getPrenoms() : "") + " " + (u.getNom() != null ? u.getNom() : "")).trim()
            : null;
        return new DestinataireInterneResponse(
            d.getId(),
            u != null ? u.getUtilisateurTrackingId() : null,
            nomComplet,
            d.getServiceLibelle(),
            d.getDateRemiseInterne(),
            d.getObservations()
        );
    }
}
