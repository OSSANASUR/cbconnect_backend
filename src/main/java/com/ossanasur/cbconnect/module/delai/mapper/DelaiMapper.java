package com.ossanasur.cbconnect.module.delai.mapper;
import com.ossanasur.cbconnect.module.delai.dto.response.*;
import com.ossanasur.cbconnect.module.delai.entity.*;
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

    public ParametreDelaiResponse toParametreResponse(ParametreDelai p) {
        if(p==null) return null;
        return new ParametreDelaiResponse(
            p.getId(), p.getCodeDelai(), p.getLibelle(),
            p.getTypeDelai(), p.getCategorie(), p.getTypeSinistre(),
            p.getValeur(), p.getUnite(), p.getReferenceJuridique(),
            p.getTauxPenalitePct(), p.getSeuilAlerte1Pct(), p.getSeuilAlerte2Pct(),
            p.isModifiable(), p.isActif()
        );
    }

    public ParametreSystemeResponse toSystemeResponse(ParametreSysteme s) {
        if(s==null) return null;
        return new ParametreSystemeResponse(
            s.getId(), s.getCle(), s.getLibelle(),
            s.getValeurDecimal(), s.getDescription(), s.isActif()
        );
    }
}
