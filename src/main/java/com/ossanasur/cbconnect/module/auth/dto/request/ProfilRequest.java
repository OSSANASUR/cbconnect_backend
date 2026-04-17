package com.ossanasur.cbconnect.module.auth.dto.request;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;
public record ProfilRequest(
    @NotBlank String profilNom,
    String commentaire,
    UUID organismeTrackingId,
    List<UUID> habilitationTrackingIds
) {}
