package com.ossanasur.cbconnect.module.pays.mapper;
import com.ossanasur.cbconnect.module.pays.dto.response.PaysResponse;
import com.ossanasur.cbconnect.module.pays.entity.Pays;
import org.springframework.stereotype.Component;
@Component
public class PaysMapper {
    public PaysResponse toResponse(Pays p) {
        if (p == null) return null;
        return new PaysResponse(p.getPaysTrackingId(), p.getCodeIso(), p.getCodeCarteBrune(),
            p.getLibelle(), p.getSmigMensuel(), p.getMonnaie(), p.getTauxChangeXof(), p.getAgeRetraite(), p.isActif());
    }
}
