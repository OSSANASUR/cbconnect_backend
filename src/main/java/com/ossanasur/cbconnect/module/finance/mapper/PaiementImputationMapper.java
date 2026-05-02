package com.ossanasur.cbconnect.module.finance.mapper;

import com.ossanasur.cbconnect.module.finance.dto.response.PaiementImputationResponse;
import com.ossanasur.cbconnect.module.finance.entity.PaiementImputation;
import org.springframework.stereotype.Component;

@Component
public class PaiementImputationMapper {

    public PaiementImputationResponse toResponse(PaiementImputation pi) {
        if (pi == null) return null;
        return new PaiementImputationResponse(
                pi.getImputationTrackingId(),
                pi.getPaiement() != null ? pi.getPaiement().getPaiementTrackingId() : null,
                pi.getEncaissement() != null ? pi.getEncaissement().getEncaissementTrackingId() : null,
                pi.getEncaissement() != null ? pi.getEncaissement().getNumeroCheque() : null,
                pi.getMontantImpute(),
                pi.getImputationOrigine() != null ? pi.getImputationOrigine().getImputationTrackingId() : null,
                pi.getCreatedAt(),
                pi.getCreatedBy()
        );
    }
}
