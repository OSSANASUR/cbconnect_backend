package com.ossanasur.cbconnect.module.reclamation.mapper;
import com.ossanasur.cbconnect.module.reclamation.dto.response.DossierReclamationResponse;
import com.ossanasur.cbconnect.module.reclamation.dto.response.FactureReclamationResponse;
import com.ossanasur.cbconnect.module.reclamation.entity.DossierReclamation;
import com.ossanasur.cbconnect.module.reclamation.entity.FactureReclamation;
import org.springframework.stereotype.Component;
@Component
public class DossierReclamationMapper {
    public DossierReclamationResponse toResponse(DossierReclamation d) {
        if(d==null) return null;
        return new DossierReclamationResponse(
            d.getDossierTrackingId(), d.getNumeroDossier(), d.getDateOuverture(), d.getDateCloture(),
            d.getStatut(), d.getMontantTotalReclame(), d.getMontantTotalRetenu(),
            d.getVictime()!=null?d.getVictime().getNom()+" "+d.getVictime().getPrenoms():null,
            d.getSinistre()!=null?d.getSinistre().getNumeroSinistreLocal():null
        );
    }
    public FactureReclamationResponse toFactureResponse(FactureReclamation f) {
        if(f==null) return null;
        return new FactureReclamationResponse(
            f.getFactureTrackingId(), f.getNumeroFactureOriginal(), f.getTypeDepense(),
            f.getNomPrestataire(), f.getDateFacture(), f.getMontantReclame(), f.getMontantRetenu(),
            f.getStatutTraitement(), f.getMotifRejet(), f.isLienAvecAccidentVerifie()
        );
    }
}
