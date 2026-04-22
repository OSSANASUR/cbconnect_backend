package com.ossanasur.cbconnect.module.sinistre.dto.request;
import com.ossanasur.cbconnect.common.enums.TypeEntiteConstat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
public record EntiteConstatRequest(
    @NotBlank String nom,
    @NotNull TypeEntiteConstat type,
    String localite,
    String codePostal,
    Boolean actif
) {}
