package com.ossanasur.cbconnect.module.finance.mapper;

import com.ossanasur.cbconnect.common.enums.StatutCheque;
import com.ossanasur.cbconnect.common.enums.TypeTable;
import com.ossanasur.cbconnect.module.auth.entity.Organisme;
import com.ossanasur.cbconnect.module.finance.dto.request.EncaissementRequest;
import com.ossanasur.cbconnect.module.finance.dto.response.EncaissementResponse;
import com.ossanasur.cbconnect.module.finance.entity.Encaissement;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class EncaissementMapper {
    public EncaissementResponse toResponse(Encaissement e) {
        if (e == null)
            return null;
        return EncaissementResponse.builder()
                .encaissementTrackingId(e.getEncaissementTrackingId())
                .numeroCheque(e.getNumeroCheque())
                .montantCheque(e.getMontantCheque())
                .montantTheorique(e.getMontantTheorique())
                .produitFraisGestion(e.getProduitFraisGestion())
                .dateEmission(e.getDateEmission())
                .dateReception(e.getDateReception())
                .dateEncaissement(e.getDateEncaissement())
                .banqueEmettrice(e.getBanqueEmettrice())
                .statutCheque(e.getStatutCheque())
                .organismeEmetteurRaisonSociale(e.getOrganismeEmetteur().getRaisonSociale())
                .sinistreNumeroLocal(e.getSinistre().getNumeroSinistreLocal())
                .chequeOrdreOrganismeNom(e.getChequeOrdreOrganisme().getRaisonSociale())
                .chequeOrdreOrganismeTrackingId(e.getChequeOrdreOrganisme().getOrganismeTrackingId())

                .build();
    }

    public Encaissement toNewEntity(
            EncaissementRequest req,
            Sinistre sinistre,
            Organisme organismeEmetteur,
            Organisme chequeOrdreOrganisme,
            String createdBy,
            StatutCheque statutCheque,
            BigDecimal fraisGestion) {
        return Encaissement.builder()
                .encaissementTrackingId(UUID.randomUUID())
                .numeroCheque(req.numeroCheque())
                .montantCheque(req.montantCheque())
                .montantTheorique(req.montantTheorique())
                .produitFraisGestion(fraisGestion)
                .dateEmission(req.dateEmission())
                .dateReception(req.dateReception())
                .banqueEmettrice(req.banqueEmettrice())
                .statutCheque(statutCheque)
                .chequeOrdreOrganisme(chequeOrdreOrganisme)
                .organismeEmetteur(organismeEmetteur)
                .sinistre(sinistre)
                .createdBy(createdBy)
                .activeData(true)
                .deletedData(false)
                .fromTable(TypeTable.ENCAISSEMENT)

                .build();
    }

}
