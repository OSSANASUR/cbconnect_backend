package com.ossanasur.cbconnect.historique;
import com.ossanasur.cbconnect.module.sinistre.dto.request.EntiteConstatRequest;
import com.ossanasur.cbconnect.module.sinistre.entity.EntiteConstat;
import com.ossanasur.cbconnect.module.sinistre.repository.EntiteConstatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;
@Service @RequiredArgsConstructor
public class EntiteConstatVersioningService extends AbstractVersioningService<EntiteConstat, EntiteConstatRequest> {
    private final EntiteConstatRepository repository;
    @Override protected JpaRepository<EntiteConstat,Integer> getRepository(){return repository;}
    @Override protected EntiteConstat findActiveByTrackingId(UUID id){return repository.findActiveByTrackingId(id).orElse(null);}
    @Override protected UUID getTrackingId(EntiteConstat e){return e.getEntiteConstatTrackingId();}
    @Override protected EntiteConstat mapToEntity(EntiteConstatRequest r, EntiteConstat ex){
        EntiteConstat u = cloneEntity(ex);
        if(r.nom()!=null) u.setNom(r.nom());
        if(r.type()!=null) u.setType(r.type());
        if(r.localite()!=null) u.setLocalite(r.localite());
        if(r.codePostal()!=null) u.setCodePostal(r.codePostal());
        if(r.actif()!=null) u.setActif(r.actif());
        return u;
    }
}
