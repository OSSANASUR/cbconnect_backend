package com.ossanasur.cbconnect.module.delai.mapper;
import com.ossanasur.cbconnect.module.delai.dto.response.NotificationDelaiResponse;
import com.ossanasur.cbconnect.module.delai.entity.NotificationDelai;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
@Component
public class DelaiMapper {
    public NotificationDelaiResponse toResponse(NotificationDelai n) {
        if(n==null) return null;
        long joursRestants = ChronoUnit.DAYS.between(LocalDate.now(), n.getDateEcheance());
        return new NotificationDelaiResponse(
            n.getId(),
            n.getParametreDelai()!=null?n.getParametreDelai().getCodeDelai():null,
            n.getParametreDelai()!=null?n.getParametreDelai().getLibelle():null,
            n.getParametreDelai()!=null?n.getParametreDelai().getReferenceJuridique():null,
            n.getSinistre()!=null?n.getSinistre().getNumeroSinistreLocal():null,
            n.getVictime()!=null?n.getVictime().getNom()+" "+n.getVictime().getPrenoms():null,
            n.getResponsable()!=null?n.getResponsable().getNom()+" "+n.getResponsable().getPrenoms():null,
            n.getDateDebut(), n.getDateEcheance(), joursRestants,
            n.getStatut(), n.getNiveauAlerte(), n.getNombreAlertes()
        );
    }
}
