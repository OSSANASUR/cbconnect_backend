package com.ossanasur.cbconnect.historique;
import com.ossanasur.cbconnect.module.auth.repository.OrganismeRepository;
import com.ossanasur.cbconnect.module.auth.repository.UtilisateurRepository;
import com.ossanasur.cbconnect.module.pays.repository.PaysRepository;
import com.ossanasur.cbconnect.module.sinistre.dto.request.SinistreRequest;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import com.ossanasur.cbconnect.module.sinistre.repository.AssureRepository;
import com.ossanasur.cbconnect.module.sinistre.repository.SinistreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;
@Service @RequiredArgsConstructor
public class SinistreVersioningService extends AbstractVersioningService<Sinistre, SinistreRequest> {
    private final SinistreRepository repository;
    private final PaysRepository paysRepository;
    private final OrganismeRepository organismeRepository;
    private final AssureRepository assureRepository;
    private final UtilisateurRepository utilisateurRepository;
    @Override protected JpaRepository<Sinistre,Integer> getRepository(){return repository;}
    @Override protected Sinistre findActiveByTrackingId(UUID id){return repository.findActiveByTrackingId(id).orElse(null);}
    @Override protected UUID getTrackingId(Sinistre e){return e.getSinistreTrackingId();}
    @Override protected Sinistre mapToEntity(SinistreRequest r, Sinistre ex){
        Sinistre u=cloneEntity(ex);
        if(r.typeSinistre()!=null)u.setTypeSinistre(r.typeSinistre());
        if(r.typeDommage()!=null)u.setTypeDommage(r.typeDommage());
        if(r.dateAccident()!=null)u.setDateAccident(r.dateAccident());
        if(r.lieuAccident()!=null)u.setLieuAccident(r.lieuAccident());
        if(r.paysEmetteurTrackingId()!=null) paysRepository.findActiveByTrackingId(r.paysEmetteurTrackingId()).ifPresent(u::setPaysEmetteur);
        if(r.organismeMembreTrackingId()!=null) organismeRepository.findActiveByTrackingId(r.organismeMembreTrackingId()).ifPresent(u::setOrganismeMembre);
        if(r.redacteurTrackingId()!=null) utilisateurRepository.findByUtilisateurTrackingIdAndActiveDataTrueAndDeletedDataFalse(r.redacteurTrackingId()).ifPresent(u::setRedacteur);
        return u;
    }
}
