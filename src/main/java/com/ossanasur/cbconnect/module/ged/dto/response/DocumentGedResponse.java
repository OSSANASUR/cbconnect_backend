package com.ossanasur.cbconnect.module.ged.dto.response;
import com.ossanasur.cbconnect.common.enums.TypeDocumentPaperless;
import java.time.LocalDate;
import java.util.UUID;
public record DocumentGedResponse(
    UUID paperlessDocumentTrackingId,
    Integer paperlessDocumentId,
    String titre,
    TypeDocumentPaperless typeDocument,
    LocalDate dateDocument,
    String mimeType,
    String sinistreNumeroLocal,
    String victimeNomPrenom
) {}
