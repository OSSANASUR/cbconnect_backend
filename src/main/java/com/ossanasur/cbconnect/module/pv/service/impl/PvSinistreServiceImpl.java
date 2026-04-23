package com.ossanasur.cbconnect.module.pv.service.impl;

import com.ossanasur.cbconnect.common.enums.SensCirculationPv;
import com.ossanasur.cbconnect.common.enums.StatutSinistre;
import com.ossanasur.cbconnect.common.enums.TypeDocumentOssanGed;
import com.ossanasur.cbconnect.common.enums.TypeSinistre;
import com.ossanasur.cbconnect.common.enums.TypeTable;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import com.ossanasur.cbconnect.exception.BadRequestException;
import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.historique.PvSinistreVersioningService;
import com.ossanasur.cbconnect.module.auth.repository.UtilisateurRepository;
import com.ossanasur.cbconnect.module.ged.service.GedAttachmentService;
import com.ossanasur.cbconnect.module.ged.service.GedAttachmentService.GedAttachmentRequest;
import com.ossanasur.cbconnect.module.ged.service.GedAttachmentService.GedAttachmentResult;
import com.ossanasur.cbconnect.module.ged.service.OssanGedClientService;
import com.ossanasur.cbconnect.module.pv.dto.request.PvSinistreRequest;
import com.ossanasur.cbconnect.module.pv.dto.response.PvSinistreResponse;
import com.ossanasur.cbconnect.module.pv.entity.PvSinistre;
import com.ossanasur.cbconnect.module.pv.mapper.PvSinistreMapper;
import com.ossanasur.cbconnect.module.pv.repository.PvSinistreRepository;
import com.ossanasur.cbconnect.module.pv.service.PvSinistreService;
import com.ossanasur.cbconnect.module.sinistre.repository.EntiteConstatRepository;
import com.ossanasur.cbconnect.module.sinistre.repository.SinistreRepository;
import com.ossanasur.cbconnect.utils.DataResponse;
import com.ossanasur.cbconnect.utils.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PvSinistreServiceImpl implements PvSinistreService {

    private static final long MAX_FILE_SIZE_BYTES = 20L * 1024 * 1024;
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final PvSinistreRepository pvRepository;
    private final SinistreRepository sinistreRepository;
    private final EntiteConstatRepository entiteConstatRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PvSinistreVersioningService versioningService;
    private final PvSinistreMapper pvMapper;
    private final GedAttachmentService gedAttachment;
    private final OssanGedClientService gedService;

    @Value("${file.upload-dir:./uploads}")
    private String uploadBaseDir;

    @Override
    @Transactional
    public DataResponse<PvSinistreResponse> enregistrer(PvSinistreRequest r, String loginAuteur) {
        if (pvRepository.existsByNumeroPvAndActiveDataTrueAndDeletedDataFalse(r.numeroPv())) {
            throw new BadRequestException("Un PV portant le numéro '" + r.numeroPv() + "' existe déjà.");
        }
        var entite = entiteConstatRepository.findActiveByTrackingId(r.entiteConstatTrackingId())
                .orElseThrow(() -> new RessourceNotFoundException("Entite constat introuvable"));
        var enregistrePar = utilisateurRepository.findByEmailAndActiveDataTrueAndDeletedDataFalse(loginAuteur)
                .orElseThrow(() -> new RessourceNotFoundException("Utilisateur introuvable"));

        Sinistre sinistreLie = null;
        if (r.sinistreTrackingId() != null) {
            sinistreLie = sinistreRepository.findActiveByTrackingId(r.sinistreTrackingId()).orElse(null);
        }

        SensCirculationPv sens = resolveSensCirculation(r.sensCirculation(), sinistreLie);

        PvSinistre pv = PvSinistre.builder()
                .pvTrackingId(UUID.randomUUID()).numeroPv(r.numeroPv())
                .sensCirculation(sens).lieuAccident(r.lieuAccident())
                .dateAccidentPv(r.dateAccidentPv()).dateReceptionBncb(r.dateReceptionBncb())
                .provenance(r.provenance()).detailProvenance(r.detailProvenance())
                .referenceSinistreLiee(r.referenceSinistreLiee())
                .aCirconstances(r.aCirconstances() != null ? r.aCirconstances() : "NEANT")
                .aAuditions(r.aAuditions() != null ? r.aAuditions() : "NEANT")
                .aCroquis(r.aCroquis() != null ? r.aCroquis() : "NEANT")
                .remarques(r.remarques()).entiteConstat(entite).enregistrePar(enregistrePar)
                .sinistre(sinistreLie)
                .createdBy(loginAuteur).activeData(true).deletedData(false).fromTable(TypeTable.PV_SINISTRE)
                .build();

        // Auto-transition : un PV reçu sur un sinistre en ATTENTE_PV passe le dossier en ATTENTE_RC.
        autoTransitionVersAttenteRc(sinistreLie, loginAuteur);

        return DataResponse.created("PV enregistre", pvMapper.toResponse(pvRepository.save(pv)));
    }

    /**
     * Règle métier : dès qu'un PV est rattaché à un sinistre au statut ATTENTE_PV,
     * le dossier bascule automatiquement en ATTENTE_RC (position RC à trancher).
     * Idempotent — n'a d'effet que si le sinistre existe et est exactement en ATTENTE_PV.
     */
    private void autoTransitionVersAttenteRc(Sinistre sinistre, String loginAuteur) {
        if (sinistre == null) return;
        if (sinistre.getStatut() == StatutSinistre.ATTENTE_PV) {
            sinistre.setStatut(StatutSinistre.ATTENTE_RC);
            sinistre.setUpdatedBy(loginAuteur);
            sinistreRepository.save(sinistre);
        }
    }

    /**
     * Règle métier : le sens de circulation du PV se déduit du sinistre quand il est associé.
     * <ul>
     *   <li>{@code TypeSinistre.SURVENU_TOGO}     → {@code SensCirculationPv.ET} (véhicule étranger au Togo)</li>
     *   <li>{@code TypeSinistre.SURVENU_ETRANGER} → {@code SensCirculationPv.TE} (véhicule togolais à l'étranger)</li>
     * </ul>
     * Si aucun sinistre n'est lié, le champ {@code sensCirculation} doit être fourni explicitement.
     */
    private SensCirculationPv resolveSensCirculation(SensCirculationPv explicite, Sinistre sinistre) {
        if (sinistre != null && sinistre.getTypeSinistre() != null) {
            return sinistre.getTypeSinistre() == TypeSinistre.SURVENU_TOGO
                    ? SensCirculationPv.ET
                    : SensCirculationPv.TE;
        }
        if (explicite != null) {
            return explicite;
        }
        throw new BadRequestException(
                "Le sens de circulation est requis lorsque le PV n'est pas associé à un sinistre.");
    }

    @Override
    @Transactional
    public DataResponse<PvSinistreResponse> modifier(UUID id, PvSinistreRequest r, String loginAuteur) {
        return DataResponse.success("PV modifie", pvMapper.toResponse(versioningService.createVersion(id, r, loginAuteur)));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<PvSinistreResponse> getByTrackingId(UUID id) {
        return DataResponse.success(pvMapper.toResponse(versioningService.getActiveVersion(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<PvSinistreResponse>> getBySinistre(UUID sinistreId) {
        return DataResponse.success(pvRepository.findBySinistre(sinistreId).stream()
                .map(pvMapper::toResponse).collect(Collectors.toList()));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<PvSinistreResponse>> getNonAssocies() {
        return DataResponse.success(pvRepository.findNonAssocies().stream()
                .map(pvMapper::toResponse).collect(Collectors.toList()));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<PvSinistreResponse> lister(int page, int size, Boolean estComplet) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        Page<PvSinistreResponse> mapped = pvRepository.findAllActive(estComplet, pageable).map(pvMapper::toResponse);
        return PaginatedResponse.fromPage(mapped, "Liste des PV");
    }

    @Override
    @Transactional
    public DataResponse<Void> associerSinistre(UUID pvId, UUID sinistreId, String loginAuteur) {
        PvSinistre pv = versioningService.getActiveVersion(pvId);
        var sinistre = sinistreRepository.findActiveByTrackingId(sinistreId)
                .orElseThrow(() -> new RessourceNotFoundException("Sinistre introuvable"));
        pv.setSinistre(sinistre);
        // Le sens de circulation du PV s'aligne sur celui du sinistre au moment de l'association.
        pv.setSensCirculation(resolveSensCirculation(pv.getSensCirculation(), sinistre));
        pv.setUpdatedBy(loginAuteur);
        pvRepository.save(pv);

        // Auto-transition statut sinistre : ATTENTE_PV → ATTENTE_RC (position RC à trancher).
        autoTransitionVersAttenteRc(sinistre, loginAuteur);

        return DataResponse.success("PV associe au sinistre", null);
    }

    @Override
    @Transactional
    public DataResponse<Void> supprimer(UUID id, String loginAuteur) {
        versioningService.softDelete(id, loginAuteur);
        return DataResponse.success("PV supprime", null);
    }

    @Override
    @Transactional
    public DataResponse<PvSinistreResponse> attacherDocument(UUID pvTrackingId, MultipartFile file, String titre,
            String typeDocument, String loginAuteur) {

        PvSinistre pv = pvRepository.findActiveByTrackingId(pvTrackingId)
                .orElseThrow(() -> new RessourceNotFoundException("PV introuvable"));

        if (pv.getSinistre() == null) {
            // Sans sinistre associé on stocke localement uniquement
            String relativePath = storeLocal(file, pvTrackingId);
            applyLocalToPv(pv, file, relativePath, loginAuteur);
            return DataResponse.created("PV enregistré (sinistre non associé — fichier en local)",
                    pvMapper.toResponse(pvRepository.save(pv)));
        }

        String titreFinal = (titre != null && !titre.isBlank()) ? titre : ("PV " + pv.getNumeroPv());
        TypeDocumentOssanGed typeGed = parseTypeDocument(typeDocument);

        GedAttachmentRequest req = new GedAttachmentRequest(
                titreFinal, typeGed, pv.getDateReceptionBncb(),
                pv.getSinistre().getSinistreTrackingId(),
                null, null, null, MAX_FILE_SIZE_BYTES, "pv");

        GedAttachmentResult r = gedAttachment.attacher(file, req, loginAuteur);

        pv.setOssanGedDocumentId(r.ossanGedDocumentId());
        pv.setOssanGedDocumentTrackingId(r.ossanGedDocumentTrackingId());
        pv.setOssanGedTaskId(r.gedTaskId());
        pv.setOssanGedIndexationStatut(r.gedIndexationStatut());
        pv.setOssanGedIndexationMessage(r.gedIndexationMessage());
        pv.setDocumentLocalPath(r.localFallbackPath());
        pv.setDocumentNomFichier(r.nomFichier());
        pv.setDocumentMimeType(r.mimeType());
        pv.setDocumentTaille(r.taille());
        pv.setDocumentUploadedAt(r.uploadedAt());
        pv.setUpdatedBy(loginAuteur);

        String message = r.ossanGedDocumentId() != null
                ? "PV archivé dans OssanGED"
                : r.ossanGedDocumentTrackingId() != null
                        ? "PV soumis à OssanGED (indexation OCR en cours)"
                        : "PV enregistré (document en attente de la GED)";
        return DataResponse.created(message, pvMapper.toResponse(pvRepository.save(pv)));
    }

    @Override
    @Transactional
    public ResponseEntity<Resource> telechargerDocument(UUID pvTrackingId) {
        PvSinistre pv = pvRepository.findActiveByTrackingId(pvTrackingId)
                .orElseThrow(() -> new RessourceNotFoundException("PV introuvable"));

        String filename = pv.getDocumentNomFichier() != null ? pv.getDocumentNomFichier() : ("PV_" + pv.getNumeroPv() + ".pdf");
        MediaType mediaType = resolveMediaType(pv.getDocumentMimeType(), filename);

        // Auto-résolution : si on a un taskId mais pas encore l'ossanGedDocumentId, on tente de le résoudre
        if (pv.getOssanGedDocumentId() == null && pv.getOssanGedDocumentTrackingId() != null) {
            try {
                var resolved = gedService.resoudreDocument(pv.getOssanGedDocumentTrackingId()).getData();
                if (resolved != null && resolved.ossanGedDocumentId() != null) {
                    pv.setOssanGedDocumentId(resolved.ossanGedDocumentId());
                    pv.setOssanGedTaskId(null);
                    pv.setOssanGedIndexationStatut("TERMINE");
                    pvRepository.save(pv);
                    log.info("PV {} : ossanGedDocumentId résolu → {}", pvTrackingId, resolved.ossanGedDocumentId());
                }
            } catch (Exception e) {
                log.debug("Auto-résolution GED pour PV {} échouée : {}", pvTrackingId, e.getMessage());
            }
        }

        if (pv.getOssanGedDocumentId() != null) {
            try {
                byte[] data = gedService.telechargerDocument(pv.getOssanGedDocumentId()).getData();
                return ResponseEntity.ok()
                        .contentType(mediaType)
                        .header(HttpHeaders.CONTENT_DISPOSITION, buildInlineDisposition(filename))
                        .body(new ByteArrayResource(data));
            } catch (Exception e) {
                log.warn("Téléchargement GED échoué pour le PV {} (docId={}) : {}. Fallback local.",
                        pvTrackingId, pv.getOssanGedDocumentId(), e.getMessage());
            }
        }

        if (pv.getDocumentLocalPath() == null || pv.getDocumentLocalPath().isBlank()) {
            throw new RessourceNotFoundException("Aucun document disponible pour ce PV");
        }

        Path path = Paths.get(uploadBaseDir).resolve(pv.getDocumentLocalPath()).normalize();
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new RessourceNotFoundException("Le fichier local du PV est introuvable");
        }

        try {
            byte[] data = Files.readAllBytes(path);
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, buildInlineDisposition(filename))
                    .body(new ByteArrayResource(data));
        } catch (IOException e) {
            throw new RuntimeException("Lecture du fichier local du PV échouée : " + e.getMessage(), e);
        }
    }

    /** Applique les métadonnées d'un stockage local au PV (cas où pas de sinistre associé). */
    private void applyLocalToPv(PvSinistre pv, MultipartFile file, String relativePath, String loginAuteur) {
        pv.setDocumentLocalPath(relativePath);
        pv.setDocumentNomFichier(file.getOriginalFilename());
        pv.setDocumentMimeType(file.getContentType());
        pv.setDocumentTaille(file.getSize());
        pv.setDocumentUploadedAt(LocalDateTime.now());
        pv.setUpdatedBy(loginAuteur);
    }

    private TypeDocumentOssanGed parseTypeDocument(String raw) {
        if (raw == null || raw.isBlank()) {
            return TypeDocumentOssanGed.PV;
        }
        try {
            return TypeDocumentOssanGed.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return TypeDocumentOssanGed.PV;
        }
    }

    private String storeLocal(MultipartFile file, UUID pvTrackingId) {
        try {
            String original = Objects.requireNonNull(file.getOriginalFilename(), "Nom de fichier requis");
            String clean = StringUtils.cleanPath(original);
            int dot = clean.lastIndexOf('.');
            String base = dot > 0 ? clean.substring(0, dot) : clean;
            String ext = dot > 0 ? clean.substring(dot) : "";
            String safe = base.replaceAll("[^a-zA-Z0-9_-]", "_")
                    + "_" + LocalDateTime.now().format(TIMESTAMP) + ext;

            Path dir = Paths.get(uploadBaseDir, "pv", pvTrackingId.toString());
            Files.createDirectories(dir);
            Path target = dir.resolve(safe);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return "pv/" + pvTrackingId + "/" + safe;
        } catch (IOException e) {
            throw new RuntimeException("Stockage local du PV échoué : " + e.getMessage(), e);
        }
    }

    private MediaType resolveMediaType(String mimeType, String filename) {
        if (mimeType != null && !mimeType.isBlank()) {
            try {
                return MediaType.parseMediaType(mimeType);
            } catch (Exception ignored) {
            }
        }
        if (filename != null && filename.toLowerCase().endsWith(".pdf")) {
            return MediaType.APPLICATION_PDF;
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    private String buildInlineDisposition(String filename) {
        return ContentDisposition.inline()
                .filename(filename, StandardCharsets.UTF_8)
                .build()
                .toString();
    }
}
