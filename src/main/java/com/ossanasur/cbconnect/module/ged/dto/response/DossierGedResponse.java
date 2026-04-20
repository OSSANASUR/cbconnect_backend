package com.ossanasur.cbconnect.module.ged.dto.response;
import com.ossanasur.cbconnect.common.enums.TypeDossierOssanGed;
import java.util.List;
import java.util.UUID;
public record DossierGedResponse(
    UUID ossanGedDossierTrackingId,
    Integer ossanGedStoragePathId,
    String cheminStockage, String titre,
    TypeDossierOssanGed typeDossier,
    String sinistreNumeroLocal,
    List<DocumentGedResponse> documents
) {}
