package com.ossanasur.cbconnect.module.sinistre.mapper;
import com.ossanasur.cbconnect.module.sinistre.dto.response.EntiteConstatResponse;
import com.ossanasur.cbconnect.module.sinistre.entity.EntiteConstat;
import org.springframework.stereotype.Component;
@Component
public class EntiteConstatMapper {
    public EntiteConstatResponse toResponse(EntiteConstat e) {
        if (e == null) return null;
        return new EntiteConstatResponse(
            e.getEntiteConstatTrackingId(), e.getNom(), e.getType(),
            e.getLocalite(), e.getCodePostal(), e.isActif()
        );
    }
}
