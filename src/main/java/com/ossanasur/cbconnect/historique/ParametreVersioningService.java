package com.ossanasur.cbconnect.historique;
import com.ossanasur.cbconnect.module.auth.dto.request.ParametreRequest;
import com.ossanasur.cbconnect.module.auth.entity.Parametre;
import com.ossanasur.cbconnect.module.auth.repository.ParametreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class ParametreVersioningService extends AbstractVersioningService<Parametre, ParametreRequest> {
    private final ParametreRepository repository;
    @Override protected JpaRepository<Parametre, Integer> getRepository() { return repository; }
    @Override protected Parametre findActiveByTrackingId(UUID id) {
        return repository.findActiveByTrackingId(id).orElse(null); }
    @Override protected UUID getTrackingId(Parametre e) { return e.getParametreTrackingId(); }
    @Override protected Parametre mapToEntity(ParametreRequest r, Parametre existing) {
        Parametre u = cloneEntity(existing);
        if (r.typeParametre() != null) u.setTypeParametre(r.typeParametre());
        if (r.cle() != null) u.setCle(r.cle());
        if (r.valeur() != null) u.setValeur(r.valeur());
        if (r.description() != null) u.setDescription(r.description());
        return u;
    }
}
