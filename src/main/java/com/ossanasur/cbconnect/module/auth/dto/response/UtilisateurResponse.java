package com.ossanasur.cbconnect.module.auth.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record UtilisateurResponse(
                UUID utilisateurTrackingId,
                String nom,
                String prenoms,
                String email,
                String username,
                String telephone,
                boolean active,
                boolean mustChangePassword,
                String profilNom,
                UUID profilTrackingId,

                UUID organismeTrackingId,
                String organismeRaisonSociale,
                String organismeCode,
                String organismeEmail,
                String organismeResponsable,
                String organismeCodePays,

                boolean twoFactorEnabled,
                LocalDateTime createdAt,
                LocalDateTime dateDeConnexion,
                LocalDateTime dateDeDeconnexion) {
}