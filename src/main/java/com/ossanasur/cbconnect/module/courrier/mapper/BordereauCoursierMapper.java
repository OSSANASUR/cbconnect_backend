package com.ossanasur.cbconnect.module.courrier.mapper;

import com.ossanasur.cbconnect.module.courrier.dto.response.BordereauCoursierResponse;
import com.ossanasur.cbconnect.module.courrier.dto.response.BordereauLigneResponse;
import com.ossanasur.cbconnect.module.courrier.entity.BordereauCoursier;
import com.ossanasur.cbconnect.module.courrier.entity.Courrier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class BordereauCoursierMapper {

    public BordereauCoursierResponse toResponse(BordereauCoursier b) {
        if (b == null) return null;

        var destOrg  = b.getDestinataireOrganisme();
        var coursier = b.getCoursier();
        String coursierNom = coursier != null
            ? ((coursier.getPrenoms() != null ? coursier.getPrenoms() : "") + " " + (coursier.getNom() != null ? coursier.getNom() : "")).trim()
            : null;

        List<BordereauLigneResponse> lignes = b.getCourriers() == null
            ? Collections.emptyList()
            : b.getCourriers().stream().map(this::toLigne).toList();

        return new BordereauCoursierResponse(
            b.getBordereauTrackingId(), b.getNumeroBordereau(),
            destOrg != null ? destOrg.getOrganismeTrackingId() : null,
            destOrg != null ? destOrg.getRaisonSociale() : null,
            b.getDestinataireLibre(),
            b.getLieuDepart(),
            b.getDateCreation(),
            b.getDateRemiseCoursier(),
            b.getDateRemiseTransporteur(),
            b.getDateDechargeRecue(),
            coursier != null ? coursier.getUtilisateurTrackingId() : null,
            coursierNom,
            b.getTransporteur(),
            b.getNomCompagnieBus(),
            b.getReferenceTransporteur(),
            b.getMontantTransporteur(),
            b.getStatut(),
            b.getDechargeGedDocumentId(),
            b.getFactureGedDocumentId(),
            b.getObservations(),
            lignes.size(),
            lignes,
            b.getCreatedBy(),
            b.getCreatedAt()
        );
    }

    public BordereauLigneResponse toLigne(Courrier c) {
        return new BordereauLigneResponse(
            c.getCourrierTrackingId(),
            c.getOrdreDansBordereau(),
            c.getSinistre() != null ? c.getSinistre().getNumeroSinistreLocal() : null,
            c.getNature(),
            c.getNumeroSinistreHomologueRef(),
            c.getReferenceCourrier(),
            c.getObjet()
        );
    }
}
