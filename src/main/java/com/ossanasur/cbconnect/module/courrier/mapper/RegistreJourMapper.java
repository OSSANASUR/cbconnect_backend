package com.ossanasur.cbconnect.module.courrier.mapper;

import com.ossanasur.cbconnect.module.courrier.dto.response.RegistreJourResponse;
import com.ossanasur.cbconnect.module.courrier.entity.RegistreJour;
import org.springframework.stereotype.Component;

@Component
public class RegistreJourMapper {

    public RegistreJourResponse toResponse(RegistreJour r) {
        if (r == null) return null;
        var sec = r.getSecretaire();
        var chef = r.getViseParUtilisateur();

        String secNom = sec != null
            ? ((sec.getPrenoms() != null ? sec.getPrenoms() : "") + " " + (sec.getNom() != null ? sec.getNom() : "")).trim()
            : null;
        String chefNom = chef != null
            ? ((chef.getPrenoms() != null ? chef.getPrenoms() : "") + " " + (chef.getNom() != null ? chef.getNom() : "")).trim()
            : null;

        int nb = r.getCourriers() == null ? 0 : r.getCourriers().size();

        return new RegistreJourResponse(
            r.getRegistreTrackingId(), r.getDateJour(), r.getTypeRegistre(), r.getStatut(),
            sec != null ? sec.getUtilisateurTrackingId() : null,
            secNom,
            r.getDateCloture(), r.getClosPar(),
            chef != null ? chef.getUtilisateurTrackingId() : null,
            chefNom,
            r.getDateVisa(), r.getCommentaireChef(),
            r.getScanGedDocumentId(),
            nb,
            r.getCreatedAt()
        );
    }
}
