package com.ossanasur.cbconnect.historique;

import com.ossanasur.cbconnect.module.auth.dto.request.BanqueRequest;
import com.ossanasur.cbconnect.module.auth.entity.Banque;
import com.ossanasur.cbconnect.module.auth.repository.BanqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BanqueVersioningService extends AbstractVersioningService<Banque, BanqueRequest> {

    private final BanqueRepository repository;

    @Override protected JpaRepository<Banque, Integer> getRepository() { return repository; }

    @Override protected Banque findActiveByTrackingId(UUID id) {
        return repository.findActiveByTrackingId(id).orElse(null);
    }

    @Override protected UUID getTrackingId(Banque e) { return e.getBanqueTrackingId(); }

    @Override protected void setTrackingId(Banque e, UUID newId) { e.setBanqueTrackingId(newId); }

    @Override protected Banque mapToEntity(BanqueRequest r, Banque existing) {
        Banque u = cloneEntity(existing);
        if (r.nom() != null)      u.setNom(r.nom());
        if (r.code() != null)     u.setCode(r.code().toUpperCase());
        if (r.codeBic() != null)  u.setCodeBic(r.codeBic().toUpperCase());
        if (r.agence() != null)   u.setAgence(r.agence());
        if (r.ville() != null)    u.setVille(r.ville());
        if (r.codePays() != null) u.setCodePays(r.codePays().toUpperCase());
        if (r.telephone() != null) u.setTelephone(r.telephone());
        return u;
    }
}
