package com.ossanasur.cbconnect.module.sinistre.dto.response;
import com.ossanasur.cbconnect.common.enums.*;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.UUID;
public record VictimeResponse(
    UUID victimeTrackingId, String nom, String prenoms, LocalDate dateNaissance, String sexe,
    String nationalite, TypeVictime typeVictime, StatutVictime statutVictime,
    StatutActivite statutActivite, BigDecimal revenuMensuel,
    boolean estDcdSuiteBlessures, LocalDate dateDeces, boolean lienDecesAccident,
    String paysResidenceLibelle, UUID sinistreTrackingId
) {}
