package com.ossanasur.cbconnect.historique;

import com.ossanasur.cbconnect.module.auth.entity.Parametre;
import com.ossanasur.cbconnect.module.pays.dto.request.PaysRequest;
import com.ossanasur.cbconnect.module.pays.entity.Pays;
import com.ossanasur.cbconnect.module.pays.repository.PaysRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaysVersioningService extends AbstractVersioningService<Pays, PaysRequest> {
    private final PaysRepository repository;

    @Override
    protected JpaRepository<Pays, Integer> getRepository() {
        return repository;
    }

    @Override
    protected Pays findActiveByTrackingId(UUID id) {
        return repository.findActiveByTrackingId(id).orElse(null);
    }

    @Override
    protected UUID getTrackingId(Pays e) {
        return e.getPaysTrackingId();
    }

    @Override
    protected Pays mapToEntity(PaysRequest r, Pays ex) {
        Pays u = cloneEntity(ex);
        if (r.codeIso() != null)
            u.setCodeIso(r.codeIso());
        if (r.codeCarteBrune() != null)
            u.setCodeCarteBrune(r.codeCarteBrune());
        if (r.libelle() != null)
            u.setLibelle(r.libelle());
        if (r.smigMensuel() != null)
            u.setSmigMensuel(r.smigMensuel());
        if (r.monnaie() != null)
            u.setMonnaie(r.monnaie());
        if (r.tauxChangeXof() != null)
            u.setTauxChangeXof(r.tauxChangeXof());
        if (r.ageRetraite() != null)
            u.setAgeRetraite(r.ageRetraite());
        u.setActif(r.actif());
        return u;
    }

    @Override
    protected void setTrackingId(Pays entity, UUID newId) {
        entity.setPaysTrackingId(newId);
    }
}
