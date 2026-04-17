package com.ossanasur.cbconnect.module.auth.mapper;
import com.ossanasur.cbconnect.module.auth.dto.response.UtilisateurResponse;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import org.springframework.stereotype.Component;

@Component
public class UtilisateurMapper {
    public UtilisateurResponse toResponse(Utilisateur u) {
        if (u == null) return null;
        return new UtilisateurResponse(
            u.getUtilisateurTrackingId(), u.getNom(), u.getPrenoms(), u.getEmail(),
            u.getUsername(), u.getTelephone(), u.isActive(), u.isMustChangePassword(),
            u.getProfil() != null ? u.getProfil().getProfilNom() : null,
            u.getProfil() != null ? u.getProfil().getProfilTrackingId() : null,
            u.getCreatedAt()
        );
    }
}
