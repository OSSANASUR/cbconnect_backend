package com.ossanasur.cbconnect.module.auth.dto.response;
import java.util.List; import java.util.UUID;
public record ProfilResponse(
    UUID profilTrackingId, String profilNom, String commentaire,
    UUID organismeTrackingId, String organismeRaisonSociale, List<HabilitationResponse> habilitations
) {}
