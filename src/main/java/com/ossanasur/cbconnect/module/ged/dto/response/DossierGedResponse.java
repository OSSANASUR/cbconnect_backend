package com.ossanasur.cbconnect.module.ged.dto.response;
import com.ossanasur.cbconnect.common.enums.TypeDossierPaperless;
import java.util.List;
import java.util.UUID;
public record DossierGedResponse(
    UUID paperlessDossierTrackingId,
    Integer paperlessStoragePathId,
    String cheminStockage, String titre,
    TypeDossierPaperless typeDossier,
    String sinistreNumeroLocal,
    List<DocumentGedResponse> documents
) {}
