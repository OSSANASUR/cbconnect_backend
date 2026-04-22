package com.ossanasur.cbconnect.historique;

import com.ossanasur.cbconnect.module.auth.dto.request.ProfilRequest;
import com.ossanasur.cbconnect.module.auth.entity.Organisme;
import com.ossanasur.cbconnect.module.auth.entity.Parametre;
import com.ossanasur.cbconnect.module.auth.entity.Profil;
import com.ossanasur.cbconnect.module.auth.repository.OrganismeRepository;
import com.ossanasur.cbconnect.module.auth.repository.ProfilRepository;
import com.ossanasur.cbconnect.security.entity.Habilitation;
import com.ossanasur.cbconnect.security.repository.HabilitationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfilVersioningService extends AbstractVersioningService<Profil, ProfilRequest> {
    private final ProfilRepository repository;
    private final OrganismeRepository organismeRepository;
    private final HabilitationRepository habilitationRepository;

    @Override
    protected JpaRepository<Profil, Integer> getRepository() {
        return repository;
    }

    @Override
    protected Profil findActiveByTrackingId(UUID id) {
        return repository.findActiveByTrackingId(id).orElse(null);
    }

    @Override
    protected UUID getTrackingId(Profil e) {
        return e.getProfilTrackingId();
    }

    @Override
    protected Profil mapToEntity(ProfilRequest r, Profil existing) {
        Profil p = cloneEntity(existing);
        if (r.profilNom() != null)
            p.setProfilNom(r.profilNom());
        if (r.commentaire() != null)
            p.setCommentaire(r.commentaire());
        if (r.organismeTrackingId() != null) {
            Organisme org = organismeRepository.findActiveByTrackingId(r.organismeTrackingId())
                    .orElseThrow(() -> new com.ossanasur.cbconnect.exception.RessourceNotFoundException(
                            "Organisme introuvable"));
            p.setOrganisme(org);
        }
        if (r.habilitationTrackingIds() != null) {
            List<Habilitation> habs = r.habilitationTrackingIds().stream()
                    .map(hid -> habilitationRepository.findActiveByTrackingId(hid)
                            .orElseThrow(() -> new com.ossanasur.cbconnect.exception.RessourceNotFoundException(
                                    "Habilitation introuvable : " + hid)))
                    .collect(Collectors.toList());
            p.setHabilitations(habs);
        }
        return p;
    }

    @Override
    protected void setTrackingId(Profil entity, UUID newId) {
        entity.setProfilTrackingId(newId);
    }
}
