package com.ossanasur.cbconnect.module.pv.mapper;
import com.ossanasur.cbconnect.module.pv.dto.response.PvSinistreResponse;
import com.ossanasur.cbconnect.module.pv.entity.PvSinistre;
import org.springframework.stereotype.Component;
@Component
public class PvSinistreMapper {
    public PvSinistreResponse toResponse(PvSinistre p) {
        if (p == null) return null;
        boolean aDocument = p.getDocumentLocalPath() != null || p.getOssanGedDocumentId() != null;
        return new PvSinistreResponse(
            p.getPvTrackingId(), p.getNumeroPv(), p.getSensCirculation(),
            p.getLieuAccident(), p.getDateAccidentPv(), p.getDateReceptionBncb(),
            p.getProvenance(), p.getDetailProvenance(), p.getReferenceSinistreLiee(),
            p.getACirconstances(), p.getAAuditions(), p.getACroquis(), p.isEstComplet(),
            p.getRemarques(),
            p.getEntiteConstat() != null ? p.getEntiteConstat().getEntiteConstatTrackingId() : null,
            p.getEntiteConstat() != null ? p.getEntiteConstat().getNom() : null,
            p.getSinistre() != null ? p.getSinistre().getSinistreTrackingId() : null,
            p.getSinistre() != null ? p.getSinistre().getNumeroSinistreLocal() : null,
            p.getDocumentNomFichier(), p.getDocumentTaille(), p.getDocumentUploadedAt(), aDocument,
            p.getOssanGedDocumentId()
        );
    }
}
