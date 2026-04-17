package com.ossanasur.cbconnect.module.courrier.mapper;
import com.ossanasur.cbconnect.module.courrier.dto.response.CourrierResponse;
import com.ossanasur.cbconnect.module.courrier.entity.Courrier;
import org.springframework.stereotype.Component;
@Component
public class CourrierMapper {
    public CourrierResponse toResponse(Courrier c) {
        if(c==null) return null;
        return new CourrierResponse(
            c.getCourrierTrackingId(), c.getReferenceCourrier(), c.getTypeCourrier(),
            c.getNature(), c.getExpediteur(), c.getDestinataire(), c.getObjet(),
            c.getDateCourrier(), c.getDateReception(), c.getCanal(),
            c.isTraite(), c.getDateTraitement(),
            c.getSinistre()!=null?c.getSinistre().getNumeroSinistreLocal():null
        );
    }
}
