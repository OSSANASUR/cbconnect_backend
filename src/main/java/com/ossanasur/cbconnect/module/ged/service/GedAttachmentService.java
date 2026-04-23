package com.ossanasur.cbconnect.module.ged.service;

import com.ossanasur.cbconnect.common.enums.TypeDocumentOssanGed;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service générique d'archivage d'un fichier dans OssanGED.
 *
 * À utiliser par tout module qui doit attacher un PDF à une entité
 * (PV, facture réclamation, courrier, expertise, attestation…).
 *
 * Encapsule :
 *  - La cascade auto de création des dossiers (sinistre / victime / reclamation)
 *  - L'indexation OssanGED + tags automatiques (type doc, EN_ATTENTE, pays)
 *  - Le fallback local en cas d'indisponibilité du moteur GED
 *  - La traçabilité (tracking UUID + ID OssanGED + métadonnées du fichier)
 *
 * L'appelant persiste ensuite les champs retournés sur sa propre entité
 * (ossan_ged_document_id, ossan_ged_document_tracking_id, nom fichier, etc.).
 */
public interface GedAttachmentService {

    GedAttachmentResult attacher(MultipartFile file, GedAttachmentRequest request, String loginAuteur);

    /**
     * Requête d'attachement : contexte + métadonnées du document.
     */
    record GedAttachmentRequest(
            String titre,
            TypeDocumentOssanGed typeDocument,
            LocalDate dateDocument,
            UUID sinistreTrackingId,
            UUID victimeTrackingId,
            UUID dossierReclamationTrackingId,
            List<String> tagsExtra,
            long maxSizeBytes,
            String fallbackDirKey
    ) {
        /**
         * Constructeur simplifié pour les cas sans taille/dossier fallback spécifique.
         */
        public GedAttachmentRequest(String titre, TypeDocumentOssanGed typeDocument, LocalDate dateDocument,
                                    UUID sinistreTrackingId, UUID victimeTrackingId, UUID dossierReclamationTrackingId,
                                    List<String> tagsExtra) {
            this(titre, typeDocument, dateDocument, sinistreTrackingId, victimeTrackingId,
                 dossierReclamationTrackingId, tagsExtra, 20L * 1024 * 1024, typeDocument != null ? typeDocument.name().toLowerCase() : "divers");
        }
    }

    /**
     * Résultat : ce que l'entité appelante doit persister.
     *
     * - ossanGedDocumentId / trackingId : non null si indexé dans OssanGED
     * - localFallbackPath : non null si GED indisponible et fichier stocké en local
     * Les deux peuvent être simultanément non null (upload réussi + copie locale de sécurité)
     * ou l'un des deux à null selon le mode de fonctionnement.
     */
    record GedAttachmentResult(
            Integer ossanGedDocumentId,
            UUID ossanGedDocumentTrackingId,
            String localFallbackPath,
            String nomFichier,
            String mimeType,
            Long taille,
            LocalDateTime uploadedAt,
            boolean gedDisponible,
            String gedTaskId,
            String gedIndexationStatut,
            String gedIndexationMessage
    ) {}
}
