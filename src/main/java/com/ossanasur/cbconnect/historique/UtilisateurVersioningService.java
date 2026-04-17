package com.ossanasur.cbconnect.historique;
import com.ossanasur.cbconnect.module.auth.dto.request.UtilisateurRequest;
import com.ossanasur.cbconnect.module.auth.entity.Profil;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.module.auth.repository.ProfilRepository;
import com.ossanasur.cbconnect.module.auth.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class UtilisateurVersioningService extends AbstractVersioningService<Utilisateur, UtilisateurRequest> {
    private final UtilisateurRepository repository;
    private final ProfilRepository profilRepository;
    @Override protected JpaRepository<Utilisateur, Integer> getRepository() { return repository; }
    @Override protected Utilisateur findActiveByTrackingId(UUID id) {
        return repository.findByUtilisateurTrackingIdAndActiveDataTrueAndDeletedDataFalse(id).orElse(null); }
    @Override protected UUID getTrackingId(Utilisateur e) { return e.getUtilisateurTrackingId(); }
    @Override protected Utilisateur mapToEntity(UtilisateurRequest r, Utilisateur existing) {
        Utilisateur u = cloneEntity(existing);
        if (r.nom() != null) u.setNom(r.nom());
        if (r.prenoms() != null) u.setPrenoms(r.prenoms());
        if (r.email() != null) u.setEmail(r.email());
        if (r.username() != null) u.setUsername(r.username());
        if (r.telephone() != null) u.setTelephone(r.telephone());
        if (r.profilTrackingId() != null) {
            Profil profil = profilRepository.findActiveByTrackingId(r.profilTrackingId())
                .orElseThrow(() -> new com.ossanasur.cbconnect.exception.RessourceNotFoundException("Profil introuvable"));
            u.setProfil(profil);
        }
        return u;
    }
}
