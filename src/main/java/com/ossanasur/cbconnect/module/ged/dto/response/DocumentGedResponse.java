package com.ossanasur.cbconnect.module.ged.dto.response;
import com.ossanasur.cbconnect.common.enums.TypeDocumentOssanGed;
import java.time.LocalDate;
import java.util.UUID;
public record DocumentGedResponse(
    UUID ossanGedDocumentTrackingId,
    Integer ossanGedDocumentId,
    String titre,
    TypeDocumentOssanGed typeDocument,
    LocalDate dateDocument,
    String mimeType,
    String sinistreNumeroLocal,
    String victimeNomPrenom
) {}
