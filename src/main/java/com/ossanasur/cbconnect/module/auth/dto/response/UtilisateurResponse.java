package com.ossanasur.cbconnect.module.auth.dto.response;
import java.time.LocalDateTime; import java.util.UUID;
public record UtilisateurResponse(
    UUID utilisateurTrackingId, String nom, String prenoms, String email,
    String username, String telephone, boolean isActive, boolean mustChangePassword,
    String profilNom, UUID profilTrackingId, LocalDateTime createdAt
) {}
