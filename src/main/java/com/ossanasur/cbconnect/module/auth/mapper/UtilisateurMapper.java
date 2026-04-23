package com.ossanasur.cbconnect.module.auth.mapper;

import com.ossanasur.cbconnect.module.auth.dto.response.UtilisateurResponse;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import org.springframework.stereotype.Component;

@Component
public class UtilisateurMapper {

    public UtilisateurResponse toResponse(Utilisateur u) {
        if (u == null)
            return null;

        return UtilisateurResponse.builder()
                .utilisateurTrackingId(u.getUtilisateurTrackingId())
                .nom(u.getNom())
                .prenoms(u.getPrenoms())
                .email(u.getEmail())
                .username(u.getUsername())
                .telephone(u.getTelephone())
                .active(u.isActive())
                .mustChangePassword(u.isMustChangePassword())
                .profilNom(u.getProfil() != null ? u.getProfil().getProfilNom() : null)
                .profilTrackingId(u.getProfil() != null ? u.getProfil().getProfilTrackingId() : null)
                .createdAt(u.getCreatedAt())
                .dateDeConnexion(u.getDateDeConnexion())
                .dateDeDeconnexion(u.getDateDeDeconnexion())

                .organismeTrackingId(u.getProfil().getOrganisme().getOrganismeTrackingId())
                .organismeRaisonSociale(u.getProfil().getOrganisme().getRaisonSociale())
                .organismeCode(u.getProfil().getOrganisme().getCode())
                .organismeEmail(u.getProfil().getOrganisme().getEmail())
                .organismeResponsable(u.getProfil().getOrganisme().getResponsable())
                .organismeCodePays(u.getProfil().getOrganisme().getCodePays())
                .twoFactorEnabled(u.getProfil().getOrganisme().isTwoFactorEnabled())

                .build();
    }
}