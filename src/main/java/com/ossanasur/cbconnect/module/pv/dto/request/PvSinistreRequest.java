package com.ossanasur.cbconnect.module.pv.dto.request;
import com.ossanasur.cbconnect.common.enums.SensCirculationPv;
import jakarta.validation.constraints.NotBlank; import jakarta.validation.constraints.NotNull;
import java.time.LocalDate; import java.util.UUID;
public record PvSinistreRequest(
    @NotBlank String numeroPv,
    // sensCirculation optionnel : dérivé du sinistre lié si présent.
    // Requis uniquement pour les PV enregistrés sans sinistre associé.
    SensCirculationPv sensCirculation,
    @NotBlank String lieuAccident, @NotNull LocalDate dateAccidentPv,
    @NotNull LocalDate dateReceptionBncb, String provenance, String detailProvenance,
    String referenceSinistreLiee,
    String aCirconstances, String aAuditions, String aCroquis, String remarques,
    @NotNull UUID entiteConstatTrackingId, UUID sinistreTrackingId
) {}
