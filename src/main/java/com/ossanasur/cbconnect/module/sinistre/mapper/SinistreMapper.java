package com.ossanasur.cbconnect.module.sinistre.mapper;
import com.ossanasur.cbconnect.module.sinistre.dto.response.SinistreResponse;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import org.springframework.stereotype.Component;
@Component
public class SinistreMapper {
    public SinistreResponse toResponse(Sinistre s) {
        if (s==null) return null;
        return new SinistreResponse(
            s.getSinistreTrackingId(), s.getNumeroSinistreLocal(), s.getNumeroSinistreHomologue(),
            s.getTypeSinistre(), s.getStatut(), s.getTypeDommage(),
            s.getDateAccident(), s.getDateDeclaration(), s.getLieuAccident(), s.isAgglomeration(),
            s.getTauxRc(), s.getPositionRc(), s.isEstPrefinance(), s.isEstContentieux(),
            s.getPaysGestionnaire()!=null?s.getPaysGestionnaire().getLibelle():null,
            s.getPaysEmetteur()!=null?s.getPaysEmetteur().getLibelle():null,
            s.getOrganismeMembre()!=null?s.getOrganismeMembre().getRaisonSociale():null,
            s.getOrganismeHomologue()!=null?s.getOrganismeHomologue().getRaisonSociale():null,
            s.getAssure()!=null?s.getAssure().getNomComplet():null,
            s.getAssure()!=null?s.getAssure().getImmatriculation():null,
            s.getRedacteur()!=null?s.getRedacteur().getNom()+" "+s.getRedacteur().getPrenoms():null,
            s.getCreatedAt()
        );
    }
}
