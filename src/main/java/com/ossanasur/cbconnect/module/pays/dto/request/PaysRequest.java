package com.ossanasur.cbconnect.module.pays.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
public record PaysRequest(
    @NotBlank String codeIso, @NotBlank String codeCarteBrune, @NotBlank String libelle,
    @NotNull BigDecimal smigMensuel, @NotBlank String monnaie,
    BigDecimal tauxChangeXof, Integer ageRetraite, boolean actif
) {}
