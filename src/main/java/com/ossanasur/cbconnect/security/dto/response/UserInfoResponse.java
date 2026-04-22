package com.ossanasur.cbconnect.security.dto.response;
import java.util.UUID;
public record UserInfoResponse(
        UUID trackingId,
        String nom,
        String prenoms,
        String email,
        String profil,
        boolean mustChangePassword,
        boolean twoFactorEnabled,
        UUID organismeTrackingId,
        String organismeRaisonSociale
) {}
