package com.ossanasur.cbconnect.module.pays.dto.response;
import java.math.BigDecimal; import java.util.UUID;
public record PaysResponse(
    UUID paysTrackingId, String codeIso, String codeCarteBrune, String libelle,
    BigDecimal smigMensuel, String monnaie, BigDecimal tauxChangeXof, Integer ageRetraite, boolean actif
) {}
