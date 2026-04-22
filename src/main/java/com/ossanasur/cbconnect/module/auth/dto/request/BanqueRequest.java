package com.ossanasur.cbconnect.module.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BanqueRequest(
    @NotBlank(message = "Le nom de la banque est obligatoire")
    String nom,

    @NotBlank(message = "Le code est obligatoire")
    @Size(max = 20, message = "Le code ne peut pas dépasser 20 caractères")
    String code,

    String codeBic,
    String agence,
    String ville,
    String codePays,
    String telephone
) {}
