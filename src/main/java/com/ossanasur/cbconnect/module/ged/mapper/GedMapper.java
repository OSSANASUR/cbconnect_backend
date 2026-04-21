package com.ossanasur.cbconnect.module.ged.mapper;

import com.ossanasur.cbconnect.module.ged.dto.response.*;
import com.ossanasur.cbconnect.module.ged.entity.*;
import org.springframework.stereotype.Component;

@Component
public class GedMapper {
    public DossierGedResponse toDossierResponse(OssanGedDossier d) {
        if (d == null)
            return null;
        return new DossierGedResponse(
                d.getOssanGedDossierTrackingId(), d.getOssanGedStoragePathId(),
                d.getCheminStockage(), d.getTitre(), d.getTypeDossier(),
                d.getSinistre() != null ? d.getSinistre().getNumeroSinistreLocal() : null, null);
    }

    public DocumentGedResponse toDocumentResponse(OssanGedDocument doc) {
        if (doc == null)
            return null;
        return new DocumentGedResponse(
                doc.getOssanGedDocumentTrackingId(), doc.getOssanGedDocumentId(),
                doc.getTitre(), doc.getTypeDocument(), doc.getDateDocument(), doc.getMimeType(),
                doc.getSinistre() != null ? doc.getSinistre().getNumeroSinistreLocal() : null,
                doc.getVictime() != null ? doc.getVictime().getNom() + " " + doc.getVictime().getPrenoms() : null);
    }
}
