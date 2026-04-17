package com.ossanasur.cbconnect.module.auth.dto.request;
import com.ossanasur.cbconnect.common.enums.TypeParametre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
public record ParametreRequest(
    @NotNull TypeParametre typeParametre,
    @NotBlank String cle,
    @NotBlank String valeur,
    String description
) {}
