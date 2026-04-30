package com.ossanasur.cbconnect.historique;

import com.ossanasur.cbconnect.module.expertise.dto.request.ExpertRequest;
import com.ossanasur.cbconnect.module.expertise.entity.Expert;
import com.ossanasur.cbconnect.module.expertise.repository.ExpertRepository;
import com.ossanasur.cbconnect.module.pays.repository.PaysRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpertVersioningService extends AbstractVersioningService<Expert, ExpertRequest> {
    private final ExpertRepository repository;
    private final PaysRepository paysRepository;

    @Override
    protected JpaRepository<Expert, Integer> getRepository() {
        return repository;
    }

    @Override
    protected Expert findActiveByTrackingId(UUID id) {
        return repository.findActiveByTrackingId(id).orElse(null);
    }

    @Override
    protected UUID getTrackingId(Expert e) {
        return e.getExpertTrackingId();
    }

    @Override
    protected Expert mapToEntity(ExpertRequest r, Expert ex) {
        Expert u = cloneEntity(ex);
        if (r.typeExpert() != null)
            u.setTypeExpert(r.typeExpert());
        if (r.nomComplet() != null)
            u.setNomComplet(r.nomComplet());
        if (r.specialite() != null)
            u.setSpecialite(r.specialite());
        if (r.nif() != null)
            u.setNif(r.nif());
        if (r.tauxRetenue() != null)
            u.setTauxRetenue(r.tauxRetenue());
        if (r.montExpertise() != null)
            u.setMontExpertise(r.montExpertise());
        u.setActif(r.actif());
        if (r.paysTrackingId() != null)
            paysRepository.findActiveByTrackingId(r.paysTrackingId()).ifPresent(u::setPays);
        return u;
    }

    @Override
    protected void setTrackingId(Expert entity, UUID newId) {
        entity.setExpertTrackingId(newId);
    }
}
