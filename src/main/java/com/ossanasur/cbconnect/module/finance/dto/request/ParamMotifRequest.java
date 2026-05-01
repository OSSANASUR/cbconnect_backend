package com.ossanasur.cbconnect.module.finance.dto.request;

import com.ossanasur.cbconnect.common.enums.TypeMotif;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ParamMotifRequest(
        @NotBlank @Size(max = 150) String libelle,
        @NotNull TypeMotif type,
        Boolean actif) {
}