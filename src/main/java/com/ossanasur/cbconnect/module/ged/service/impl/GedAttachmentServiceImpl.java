package com.ossanasur.cbconnect.module.ged.service.impl;

import com.ossanasur.cbconnect.exception.BadRequestException;
import com.ossanasur.cbconnect.module.ged.dto.request.UploadDocumentRequest;
import com.ossanasur.cbconnect.module.ged.dto.response.DocumentGedResponse;
import com.ossanasur.cbconnect.module.ged.service.GedAttachmentService;
import com.ossanasur.cbconnect.module.ged.service.OssanGedClientService;
import com.ossanasur.cbconnect.utils.DataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Implémentation du service générique d'attachement GED.
 * - Tente d'abord l'upload via OssanGedClientService (cascade sinistre/victime/reclamation)
 * - Fallback local si la GED est indisponible
 * - Retourne toujours un résultat structuré que l'entité appelante peut persister
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GedAttachmentServiceImpl implements GedAttachmentService {

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final String PDF_MIME = "application/pdf";

    private final OssanGedClientService gedClient;

    @Value("${file.upload-dir:./uploads}")
    private String uploadBaseDir;

    @Override
    public GedAttachmentResult attacher(MultipartFile file, GedAttachmentRequest req, String loginAuteur) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Le fichier est requis");
        }
        if (file.getSize() > req.maxSizeBytes()) {
            throw new BadRequestException("Le fichier dépasse la taille autorisée ("
                    + (req.maxSizeBytes() / (1024 * 1024)) + " Mo)");
        }
        // Seul le PDF est accepté pour archivage GED
        if (!PDF_MIME.equalsIgnoreCase(file.getContentType())
                && !isPdfExtension(file.getOriginalFilename())) {
            throw new BadRequestException("Format non accepté : seuls les PDF sont archivés dans la GED");
        }
        if (req.sinistreTrackingId() == null) {
            throw new BadRequestException("Le sinistre doit être identifié pour archiver le document");
        }

        LocalDateTime now = LocalDateTime.now();

        // 1) Indexation dans OssanGED via le client (cascade auto de dossiers)
        try {
            UploadDocumentRequest upload = new UploadDocumentRequest(
                    req.titre(),
                    req.typeDocument(),
                    req.dateDocument(),
                    req.sinistreTrackingId(),
                    req.victimeTrackingId(),
                    req.dossierReclamationTrackingId(),
                    null,
                    req.tagsExtra());
            DataResponse<DocumentGedResponse> res = gedClient.uploadDocument(file, upload, loginAuteur);
            DocumentGedResponse d = res != null ? res.getData() : null;
            if (d != null && d.ossanGedDocumentId() != null) {
                log.info("Document archivé dans OssanGED : titre={}, id={}", req.titre(), d.ossanGedDocumentId());
                return new GedAttachmentResult(
                        d.ossanGedDocumentId(),
                        d.ossanGedDocumentTrackingId(),
                        null,
                        file.getOriginalFilename(),
                        file.getContentType(),
                        file.getSize(),
                        now,
                        true);
            }
        } catch (BadRequestException bre) {
            // Erreur utilisateur : on remonte sans fallback
            throw bre;
        } catch (Exception e) {
            log.warn("OssanGED indisponible, fallback local : {}", e.getMessage());
        }

        // 2) Fallback : stockage local
        String relativePath = storeLocal(file, req);
        log.info("Document stocké en local (GED indisponible) : {}", relativePath);
        return new GedAttachmentResult(
                null,
                null,
                relativePath,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                now,
                false);
    }

    private boolean isPdfExtension(String filename) {
        return filename != null && filename.toLowerCase().endsWith(".pdf");
    }

    private String storeLocal(MultipartFile file, GedAttachmentRequest req) {
        try {
            String original = Objects.requireNonNull(file.getOriginalFilename(), "Nom de fichier requis");
            String clean = StringUtils.cleanPath(original);
            int dot = clean.lastIndexOf('.');
            String base = dot > 0 ? clean.substring(0, dot) : clean;
            String ext = dot > 0 ? clean.substring(dot) : "";
            String safe = base.replaceAll("[^a-zA-Z0-9_-]", "_")
                    + "_" + LocalDateTime.now().format(TIMESTAMP) + ext;

            String sinistreKey = req.sinistreTrackingId() != null
                    ? req.sinistreTrackingId().toString() : "divers";
            Path dir = Paths.get(uploadBaseDir, req.fallbackDirKey(), sinistreKey);
            Files.createDirectories(dir);
            Path target = dir.resolve(safe);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return req.fallbackDirKey() + "/" + sinistreKey + "/" + safe;
        } catch (IOException e) {
            throw new RuntimeException("Stockage local du document échoué : " + e.getMessage(), e);
        }
    }
}
