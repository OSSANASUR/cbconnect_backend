package com.ossanasur.cbconnect.historique;

import com.ossanasur.cbconnect.module.pays.repository.PaysRepository;
import com.ossanasur.cbconnect.module.sinistre.dto.request.VictimeRequest;
import com.ossanasur.cbconnect.module.sinistre.entity.Victime;
import com.ossanasur.cbconnect.module.sinistre.repository.VictimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VictimeVersioningService extends AbstractVersioningService<Victime, VictimeRequest> {
    private final VictimeRepository repository;
    private final PaysRepository paysRepository;

    @Override
    protected JpaRepository<Victime, Integer> getRepository() {
        return repository;
    }

    @Override
    protected Victime findActiveByTrackingId(UUID id) {
        return repository.findActiveByTrackingId(id).orElse(null);
    }

    @Override
    protected UUID getTrackingId(Victime e) {
        return e.getVictimeTrackingId();
    }

    @Override
    protected Victime mapToEntity(VictimeRequest r, Victime ex) {
        Victime u = cloneEntity(ex);
        if (r.nom() != null)
            u.setNom(r.nom());
        if (r.prenoms() != null)
            u.setPrenoms(r.prenoms());
        if (r.dateNaissance() != null)
            u.setDateNaissance(r.dateNaissance());
        if (r.sexe() != null)
            u.setSexe(r.sexe());
        if (r.nationalite() != null)
            u.setNationalite(r.nationalite());
        if (r.statutActivite() != null)
            u.setStatutActivite(r.statutActivite());
        if (r.revenuMensuel() != null)
            u.setRevenuMensuel(r.revenuMensuel());
        if (r.paysResidenceTrackingId() != null)
            paysRepository.findActiveByTrackingId(r.paysResidenceTrackingId()).ifPresent(u::setPaysResidence);
        return u;
    }

    @Override
    protected void setTrackingId(Victime entity, UUID newId) {
        entity.setVictimeTrackingId(newId);
    }
}
