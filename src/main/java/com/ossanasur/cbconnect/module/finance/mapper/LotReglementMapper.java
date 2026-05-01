package com.ossanasur.cbconnect.module.finance.mapper;

import com.ossanasur.cbconnect.module.expertise.entity.Expert;
import com.ossanasur.cbconnect.module.finance.dto.response.LotReglementResponse;
import com.ossanasur.cbconnect.module.finance.entity.LotReglement;
import com.ossanasur.cbconnect.module.finance.entity.Paiement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LotReglementMapper {

    private final PaiementMapper paiementMapper;

    public LotReglementResponse toResponse(LotReglement lot, List<Paiement> paiements) {
        Expert e = lot.getExpert();
        return new LotReglementResponse(
                lot.getLotTrackingId(),
                lot.getNumeroLot(),
                e != null ? e.getExpertTrackingId() : null,
                e != null ? e.getNomComplet() : null,
                lot.getTauxRetenue(),
                lot.getStatut(),
                lot.getNombreReglements(),
                lot.getMontantTtcTotal(),
                lot.getMontantTvaTotal(),
                lot.getMontantTaxeTotal(),
                lot.getNumeroChequeGlobal(),
                lot.getBanqueCheque(),
                lot.getDateEmissionCheque(),
                paiements.stream().map(paiementMapper::toResponse).toList(),
                lot.getCreatedAt(),
                lot.getCreatedBy()
        );
    }
}
