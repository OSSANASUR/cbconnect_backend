package com.ossanasur.cbconnect.module.ged.mapper;

import com.ossanasur.cbconnect.module.ged.dto.response.*;
import com.ossanasur.cbconnect.module.ged.entity.*;
import org.springframework.stereotype.Component;

@Component
public class GedMapper {
    public DossierGedResponse toDossierResponse(OssanGedDossier d) {
        if (d == null)
            return null;
        // Chemin : plusieurs formats historiques
        //   Sinistres/{annee}/{ET|TE}/...       (nouveau)
        //   Sinistres/{ET|TE}/{annee}/...       (ancien)
        //   Sinistres/{ET|TE}/{bureau}/{num}/.../{annee}  (BNCB ancien)
        String chemin = d.getCheminStockage();
        String[] parts = chemin != null ? chemin.split("/") : new String[0];
        Integer annee = null;
        String typeDir = null;
        if (parts.length >= 2) {
            String p1 = parts[1];
            if (p1.matches("\\d{4}")) {
                // Nouveau format : parts[1]=année, parts[2]=type
                annee = Integer.parseInt(p1);
                if (parts.length >= 3) typeDir = parts[2];
            } else {
                // Ancien format : parts[1]=type (ET/TE), chercher l'année
                typeDir = p1;
                if (parts.length >= 3) {
                    try { annee = Integer.parseInt(parts[2]); }
                    catch (NumberFormatException ignored) {
                        // BNCB : année dans le dernier segment
                        String last = parts[parts.length - 1];
                        try { annee = Integer.parseInt(last); } catch (NumberFormatException ignored2) {}
                    }
                }
            }
        }
        return new DossierGedResponse(
                d.getOssanGedDossierTrackingId(), d.getOssanGedStoragePathId(),
                chemin, d.getTitre(), d.getTypeDossier(),
                d.getSinistre() != null ? d.getSinistre().getNumeroSinistreLocal() : null,
                annee, typeDir, null);
    }

    public DocumentGedResponse toDocumentResponse(OssanGedDocument doc) {
        if (doc == null)
            return null;
        String statut = doc.getOssanGedDocumentId() != null ? "TERMINE" : "EN_COURS";
        return new DocumentGedResponse(
                doc.getOssanGedDocumentTrackingId(), doc.getOssanGedDocumentId(),
                doc.getTitre(), doc.getTypeDocument(), doc.getDateDocument(), doc.getMimeType(),
                doc.getSinistre() != null ? doc.getSinistre().getNumeroSinistreLocal() : null,
                doc.getVictime() != null ? doc.getVictime().getNom() + " " + doc.getVictime().getPrenoms() : null,
                doc.getGedTaskId(),
                statut,
                null);
    }
}
