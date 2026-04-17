package com.ossanasur.cbconnect.module.pv.dto.response;
import com.ossanasur.cbconnect.common.enums.SensCirculationPv;
import java.time.LocalDate; import java.util.UUID;
public record PvSinistreResponse(
    UUID pvTrackingId, String numeroPv, SensCirculationPv sensCirculation,
    String lieuAccident, LocalDate dateAccidentPv, LocalDate dateReceptionBncb,
    String provenance, String referenceSinistreLiee,
    String aCirconstances, String aAuditions, String aCroquis, boolean estComplet,
    String remarques, String entiteConstatNom, String sinistreNumeroLocal
) {}
