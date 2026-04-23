package com.ossanasur.cbconnect.module.pv.dto.response;

import com.ossanasur.cbconnect.common.enums.SensCirculationPv;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PvSinistreResponse(
        UUID pvTrackingId, String numeroPv, SensCirculationPv sensCirculation,
        String lieuAccident, LocalDate dateAccidentPv, LocalDate dateReceptionBncb,
        String provenance, String detailProvenance, String referenceSinistreLiee,
        String aCirconstances, String aAuditions, String aCroquis, boolean estComplet,
        String remarques,
        UUID entiteConstatTrackingId, String entiteConstatNom,
        UUID sinistreTrackingId, String numeroSinistreLocal,
        String documentNomFichier, Long documentTaille, LocalDateTime documentUploadedAt, boolean aDocument,
        Integer ossanGedDocumentId,
        String ossanGedTaskId,
        String ossanGedIndexationStatut,
        String ossanGedIndexationMessage) {
}
