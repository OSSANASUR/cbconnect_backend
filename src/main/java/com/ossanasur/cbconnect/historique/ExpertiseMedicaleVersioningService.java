package com.ossanasur.cbconnect.historique;

import com.ossanasur.cbconnect.module.expertise.dto.request.ExpertiseMedicaleRequest;
import com.ossanasur.cbconnect.module.expertise.entity.ExpertiseMedicale;
import com.ossanasur.cbconnect.module.expertise.repository.ExpertRepository;
import com.ossanasur.cbconnect.module.expertise.repository.ExpertiseMedicaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpertiseMedicaleVersioningService
        extends AbstractVersioningService<ExpertiseMedicale, ExpertiseMedicaleRequest> {
    private final ExpertiseMedicaleRepository repository;
    private final ExpertRepository expertRepository;

    @Override
    protected JpaRepository<ExpertiseMedicale, Integer> getRepository() {
        return repository;
    }

    @Override
    protected ExpertiseMedicale findActiveByTrackingId(UUID id) {
        return repository.findActiveByTrackingId(id).orElse(null);
    }

    @Override
    protected UUID getTrackingId(ExpertiseMedicale e) {
        return e.getExpertiseMedTrackingId();
    }

    @Override
    protected ExpertiseMedicale mapToEntity(ExpertiseMedicaleRequest r, ExpertiseMedicale ex) {
        ExpertiseMedicale u = cloneEntity(ex);
        if (r.dateRapport() != null)
            u.setDateRapport(r.dateRapport());
        if (r.dateConsolidation() != null)
            u.setDateConsolidation(r.dateConsolidation());
        if (r.tauxIpp() != null)
            u.setTauxIpp(r.tauxIpp());
        if (r.dureeIttJours() != null)
            u.setDureeIttJours(r.dureeIttJours());
        if (r.pretiumDoloris() != null)
            u.setPretiumDoloris(r.pretiumDoloris());
        if (r.prejudiceEsthetique() != null)
            u.setPrejudiceEsthetique(r.prejudiceEsthetique());
        if (r.necessiteTiercePersonne() != null)
            u.setNecessiteTiercePersonne(r.necessiteTiercePersonne());
        if (r.honoraires() != null)
            u.setHonoraires(r.honoraires());
        if (r.honorairesContreExpertise() != null)
            u.setHonorairesContreExpertise(r.honorairesContreExpertise());
        if (r.ossanGedDocumentId() != null)
            u.setOssanGedDocumentId(r.ossanGedDocumentId());
        if (r.expertTrackingId() != null)
            expertRepository.findActiveByTrackingId(r.expertTrackingId()).ifPresent(u::setExpert);
        return u;
    }

    @Override
    protected void setTrackingId(ExpertiseMedicale entity, UUID newId) {
        entity.setExpertiseMedTrackingId(newId);
    }
}