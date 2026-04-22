package com.ossanasur.cbconnect.module.sinistre.dto.response;
import com.ossanasur.cbconnect.common.enums.TypeEntiteConstat;
import java.util.UUID;
public record EntiteConstatResponse(
    UUID entiteConstatTrackingId, String nom, TypeEntiteConstat type,
    String localite, String codePostal, boolean actif
) {}
