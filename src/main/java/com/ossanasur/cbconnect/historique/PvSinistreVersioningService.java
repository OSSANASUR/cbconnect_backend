package com.ossanasur.cbconnect.historique;
import com.ossanasur.cbconnect.module.pv.dto.request.PvSinistreRequest;
import com.ossanasur.cbconnect.module.pv.entity.PvSinistre;
import com.ossanasur.cbconnect.module.pv.repository.PvSinistreRepository;
import com.ossanasur.cbconnect.module.sinistre.repository.EntiteConstatRepository;
import com.ossanasur.cbconnect.module.sinistre.repository.SinistreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;
@Service @RequiredArgsConstructor
public class PvSinistreVersioningService extends AbstractVersioningService<PvSinistre, PvSinistreRequest> {
    private final PvSinistreRepository repository;
    private final EntiteConstatRepository entiteConstatRepository;
    private final SinistreRepository sinistreRepository;
    @Override protected JpaRepository<PvSinistre,Integer> getRepository(){return repository;}
    @Override protected PvSinistre findActiveByTrackingId(UUID id){return repository.findActiveByTrackingId(id).orElse(null);}
    @Override protected UUID getTrackingId(PvSinistre e){return e.getPvTrackingId();}
    @Override protected PvSinistre mapToEntity(PvSinistreRequest r, PvSinistre ex){
        PvSinistre u=cloneEntity(ex);
        if(r.numeroPv()!=null) u.setNumeroPv(r.numeroPv());
        if(r.lieuAccident()!=null) u.setLieuAccident(r.lieuAccident());
        if(r.dateAccidentPv()!=null) u.setDateAccidentPv(r.dateAccidentPv());
        if(r.dateReceptionBncb()!=null) u.setDateReceptionBncb(r.dateReceptionBncb());
        if(r.provenance()!=null) u.setProvenance(r.provenance());
        if(r.aCirconstances()!=null) u.setACirconstances(r.aCirconstances());
        if(r.aAuditions()!=null) u.setAAuditions(r.aAuditions());
        if(r.aCroquis()!=null) u.setACroquis(r.aCroquis());
        if(r.remarques()!=null) u.setRemarques(r.remarques());
        if(r.entiteConstatTrackingId()!=null) entiteConstatRepository.findActiveByTrackingId(r.entiteConstatTrackingId()).ifPresent(u::setEntiteConstat);
        if(r.sinistreTrackingId()!=null) sinistreRepository.findActiveByTrackingId(r.sinistreTrackingId()).ifPresent(u::setSinistre);
        return u;
    }
}
