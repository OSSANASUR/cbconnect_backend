package com.ossanasur.cbconnect.module.finance.mapper;
import com.ossanasur.cbconnect.module.finance.dto.response.EncaissementResponse;
import com.ossanasur.cbconnect.module.finance.entity.Encaissement;
import org.springframework.stereotype.Component;
@Component
public class EncaissementMapper {
    public EncaissementResponse toResponse(Encaissement e) {
        if(e==null) return null;
        return new EncaissementResponse(
            e.getEncaissementTrackingId(), e.getNumeroCheque(), e.getMontantCheque(), e.getMontantTheorique(),
            e.getProduitFraisGestion(), e.getDateEmission(), e.getDateReception(), e.getDateEncaissement(),
            e.getBanqueEmettrice(), e.getStatutCheque(),
            e.getOrganismeEmetteur()!=null?e.getOrganismeEmetteur().getRaisonSociale():null,
            e.getSinistre()!=null?e.getSinistre().getNumeroSinistreLocal():null,
            e.getChequeOrdreOrganisme()!=null?e.getChequeOrdreOrganisme().getRaisonSociale():null,
            e.getChequeOrdreOrganisme()!=null?e.getChequeOrdreOrganisme().getOrganismeTrackingId():null
        );
    }
}
