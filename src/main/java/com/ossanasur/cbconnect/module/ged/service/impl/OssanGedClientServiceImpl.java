package com.ossanasur.cbconnect.module.ged.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ossanasur.cbconnect.common.enums.StatutDossierReclamation;
import com.ossanasur.cbconnect.common.enums.TypeDocumentOssanGed;
import com.ossanasur.cbconnect.common.enums.TypeDossierOssanGed;
import com.ossanasur.cbconnect.common.enums.TypeSinistre;
import com.ossanasur.cbconnect.common.enums.TypeTable;
import com.ossanasur.cbconnect.config.OssanGedConfig;
import com.ossanasur.cbconnect.exception.BadRequestException;
import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.module.auth.repository.UtilisateurRepository;
import com.ossanasur.cbconnect.module.ged.dto.request.UploadDocumentRequest;
import com.ossanasur.cbconnect.module.ged.dto.response.DocumentGedResponse;
import com.ossanasur.cbconnect.module.ged.dto.response.DossierGedResponse;
import com.ossanasur.cbconnect.module.ged.entity.OssanGedDocument;
import com.ossanasur.cbconnect.module.ged.entity.OssanGedDossier;
import com.ossanasur.cbconnect.module.ged.mapper.GedMapper;
import com.ossanasur.cbconnect.module.ged.repository.OssanGedDocumentRepository;
import com.ossanasur.cbconnect.module.ged.repository.OssanGedDossierRepository;
import com.ossanasur.cbconnect.module.ged.service.OssanGedClientService;
import com.ossanasur.cbconnect.module.reclamation.entity.DossierReclamation;
import com.ossanasur.cbconnect.module.reclamation.repository.DossierReclamationRepository;
import com.ossanasur.cbconnect.module.reclamation.service.PiecesAdministrativesService;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import com.ossanasur.cbconnect.module.sinistre.entity.Victime;
import com.ossanasur.cbconnect.module.sinistre.repository.SinistreRepository;
import com.ossanasur.cbconnect.module.sinistre.repository.VictimeRepository;
import com.ossanasur.cbconnect.utils.DataResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Proxy Spring Boot vers OssanGED (moteur GED reconfiguré).
 * Le frontend ne communique JAMAIS directement avec OssanGED.
 *
 * Hiérarchie des dossiers :
 * Sinistres/<année>/<ET|TE>/<NumeroSinistre>/<NOM_PRENOMS_Victime>/<NumeroDossierReclamation>/
 *
 * Création en cascade automatique à l'upload si un niveau manque.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OssanGedClientServiceImpl implements OssanGedClientService {
    private static final Pattern DUPLICATE_DOCUMENT_PATTERN = Pattern.compile("duplicate of .*\\(#(\\d+)\\)", Pattern.CASE_INSENSITIVE);
    private record UploadLaunchResult(Integer documentId, String taskId, String status, String message) {}

    private final OssanGedConfig ossanGedConfig;
    private final OkHttpClient ossanGedHttpClient;
    private final ObjectMapper objectMapper;
    private final OssanGedDossierRepository dossierRepository;
    private final OssanGedDocumentRepository documentRepository;
    private final SinistreRepository sinistreRepository;
    private final VictimeRepository victimeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final DossierReclamationRepository dossierReclamationRepository;
    private final GedMapper gedMapper;
    private final PiecesAdministrativesService piecesService;

    @Value("${ossanged.upload-task-wait-seconds:240}")
    private long uploadTaskWaitSeconds;

    @Value("${ossanged.upload-task-poll-interval-millis:2000}")
    private long uploadTaskPollIntervalMillis;

    @Value("${ossanged.upload-inline-wait-seconds:5}")
    private long uploadInlineWaitSeconds;

    private String authToken;
    private final Map<String, Integer> tagCache = new HashMap<>();

    // ── Init ──────────────────────────────────────────────────────────────────

    @PostConstruct
    public void init() {
        try {
            if (ossanGedConfig.getApiToken() != null && !ossanGedConfig.getApiToken().isBlank()) {
                authToken = ossanGedConfig.getApiToken();
                log.info("OssanGED : token API configuré via properties");
            } else {
                authToken = obtainToken();
                log.info("OssanGED : token API obtenu par authentification");
            }
            chargerCacheTags();
        } catch (Exception e) {
            log.warn("OssanGED indisponible au démarrage — mode dégradé actif : {}", e.getMessage());
        }
    }

    @Override
    public void initTagsOssanGed() {
        try {
            chargerCacheTags();
            log.info("OssanGED : cache des tags rechargé ({} tags)", tagCache.size());
        } catch (Exception e) {
            log.warn("Impossible de recharger le cache des tags OssanGED : {}", e.getMessage());
        }
    }

    // ── Création de dossiers (publics) ────────────────────────────────────────

    @Override
    @Transactional
    public DataResponse<DossierGedResponse> creerDossierSinistre(UUID sinistreId, String loginAuteur) {
        var sinistre = sinistreRepository.findActiveByTrackingId(sinistreId)
                .orElseThrow(() -> new RessourceNotFoundException("Sinistre introuvable"));
        OssanGedDossier dossier = getOrCreateDossierSinistre(sinistre, loginAuteur);
        return DataResponse.success("Dossier sinistre OssanGED", gedMapper.toDossierResponse(dossier));
    }

    @Override
    @Transactional
    public DataResponse<DossierGedResponse> creerDossierVictime(UUID victimeId, String loginAuteur) {
        var victime = victimeRepository.findActiveByTrackingId(victimeId)
                .orElseThrow(() -> new RessourceNotFoundException("Victime introuvable"));
        OssanGedDossier dossier = getOrCreateDossierVictime(victime, loginAuteur);
        return DataResponse.success("Dossier victime OssanGED", gedMapper.toDossierResponse(dossier));
    }

    @Override
    @Transactional
    public DataResponse<DossierGedResponse> creerDossierReclamation(UUID dossierReclamationId, String loginAuteur) {
        var dr = dossierReclamationRepository.findActiveByTrackingId(dossierReclamationId)
                .orElseThrow(() -> new RessourceNotFoundException("Dossier de reclamation introuvable"));
        OssanGedDossier dossier = getOrCreateDossierReclamation(dr, loginAuteur);
        return DataResponse.success("Dossier reclamation OssanGED", gedMapper.toDossierResponse(dossier));
    }

    // ── Upload ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public DataResponse<DocumentGedResponse> uploadDocument(MultipartFile file, UploadDocumentRequest r,
            String loginAuteur) {
        if (file.isEmpty())
            throw new BadRequestException("Fichier vide");
        if (r.sinistreTrackingId() == null)
            throw new BadRequestException("sinistreTrackingId requis");

        var sinistre = sinistreRepository.findActiveByTrackingId(r.sinistreTrackingId())
                .orElseThrow(() -> new RessourceNotFoundException("Sinistre introuvable"));

        OssanGedDossier dossier = getOrCreateDossierSinistre(sinistre, loginAuteur);
        Victime victime = null;
        DossierReclamation dr = null;

        if (r.victimeTrackingId() != null) {
            victime = victimeRepository.findActiveByTrackingId(r.victimeTrackingId())
                    .orElseThrow(() -> new RessourceNotFoundException("Victime introuvable"));
            dossier = getOrCreateDossierVictime(victime, loginAuteur);

            if (r.dossierReclamationTrackingId() != null) {
                dr = dossierReclamationRepository.findActiveByTrackingId(r.dossierReclamationTrackingId())
                        .orElseThrow(() -> new RessourceNotFoundException("Dossier reclamation introuvable"));
            } else {
                var list = dossierReclamationRepository.findByVictime(victime.getVictimeTrackingId());
                dr = list.isEmpty() ? createDefaultDossierReclamation(sinistre, victime, loginAuteur) : list.get(0);
            }
            dossier = getOrCreateDossierReclamation(dr, loginAuteur);
        }

        List<Integer> tagIds = resolverTagsDocument(r.typeDocument(), sinistre, r.tagsExtra(), r.tagIds());

        var uploader = utilisateurRepository.findByEmailAndActiveDataTrueAndDeletedDataFalse(loginAuteur).orElse(null);

        try {
            UploadLaunchResult upload = uploadToOssanGed(file, r.titre(),
                    dossier.getOssanGedStoragePathId(),
                    dossier.getOssanGedCorrespondentId(),
                    tagIds);

            OssanGedDocument doc = OssanGedDocument.builder()
                    .ossanGedDocumentTrackingId(UUID.randomUUID())
                    .ossanGedDocumentId(upload.documentId())
                    .gedTaskId(upload.documentId() == null ? upload.taskId() : null)
                    .titre(r.titre())
                    .typeDocument(r.typeDocument())
                    .dateDocument(r.dateDocument())
                    .mimeType(file.getContentType())
                    .dossier(dossier)
                    .sinistre(sinistre)
                    .victime(victime)
                    .uploadePar(uploader)
                    .createdBy(loginAuteur).activeData(true).deletedData(false)
                    .fromTable(TypeTable.OSSAN_GED_DOCUMENT)
                    .build();
            DocumentGedResponse saved = gedMapper.toDocumentResponse(documentRepository.save(doc));

            // Auto-association dès l'upload (qu'OCR soit résolu ou en cours).
            // L'OssanGedDocument existe en DB avec son trackingId : la pièce peut être
            // marquée RECUE immédiatement ; ossanGedDocumentId sera renseigné
            // ultérieurement par resoudreDocument() quand l'OCR Paperless termine.
            if (dr != null) {
                log.info("[AUTO-ASSOC] Déclenchement upload — dossier={} typeDoc={} victimeId={}",
                        dr.getDossierTrackingId(), r.typeDocument(), r.victimeTrackingId());
                piecesService.autoAssocierParTypeDocument(
                        dr.getDossierTrackingId(), r.typeDocument(),
                        doc.getOssanGedDocumentTrackingId(), loginAuteur);
            } else {
                log.warn("[AUTO-ASSOC] Ignoré — dr null (victimeTrackingId={}). "
                        + "Sélectionner une victime dans le GedUploader pour activer l'auto-association.",
                        r.victimeTrackingId());
            }

            if (upload.documentId() != null) {
                log.info("Document indexe dans OssanGED : id={}, titre={}", upload.documentId(), r.titre());
                return DataResponse.created("Document indexe dans OssanGED", saved);
            }
            log.info("Document uploade dans OssanGED (OCR en cours) : taskId={}, tracking={}", upload.taskId(), doc.getOssanGedDocumentTrackingId());
            return DataResponse.success("Indexation OssanGED en cours", saved);
        } catch (Exception e) {
            log.warn("Upload OssanGED echoue (document non soumis a la GED) : {}", e.getMessage());
            throw new RuntimeException("Upload OssanGED echoue : " + e.getMessage(), e);
        }
    }

    @Override
    public DataResponse<byte[]> telechargerDocument(Integer ossanGedDocumentId) {
        System.out.println("Téléchargement document OssanGED : id=" + ossanGedDocumentId);
        try {
            return DataResponse.success(downloadFromOssanGed(ossanGedDocumentId));
        } catch (Exception e) {
            throw new RuntimeException("Impossible de telecharger depuis OssanGED : " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public DataResponse<String> repairerStoragePaths() {
        List<OssanGedDossier> dossiers = dossierRepository.findAllSinistreDossiers();
        int repaired = 0;
        int skipped  = 0;
        for (OssanGedDossier d : dossiers) {
            try {
                Integer correctId = tryCreateStoragePath(d.getCheminStockage());
                if (correctId == null) { skipped++; continue; }
                if (!correctId.equals(d.getOssanGedStoragePathId())) {
                    d.setOssanGedStoragePathId(correctId);
                    dossierRepository.save(d);
                    repaired++;
                    log.info("StoragePath réparé : {} → id={}", d.getCheminStockage(), correctId);
                }
            } catch (Exception e) {
                log.warn("Impossible de réparer storage path pour {} : {}", d.getCheminStockage(), e.getMessage());
                skipped++;
            }
        }
        String msg = String.format("Réparation terminée : %d corrigé(s), %d ignoré(s) sur %d dossiers",
                repaired, skipped, dossiers.size());
        log.info(msg);
        return DataResponse.success(msg);
    }

    @Override
    @Transactional
    public DataResponse<String> migrerDocumentsStoragePaths() {
        List<OssanGedDocument> docs = documentRepository.findAllWithPaperlessId();
        int migrated = 0, skipped = 0;
        for (OssanGedDocument doc : docs) {
            try {
                OssanGedDossier dossier = doc.getDossier();
                if (dossier == null || dossier.getOssanGedStoragePathId() == null) { skipped++; continue; }
                Integer targetSpId = dossier.getOssanGedStoragePathId();
                // PATCH le document dans Paperless pour mettre à jour le storage_path
                String patchBody = objectMapper.writeValueAsString(Map.of("storage_path", targetSpId));
                String url = ossanGedConfig.getBaseUrl() + "/api/documents/" + doc.getOssanGedDocumentId() + "/";
                Request req = new Request.Builder().url(url)
                        .header("Authorization", "Token " + authToken)
                        .patch(RequestBody.create(patchBody, MediaType.get("application/json"))).build();
                try (Response res = ossanGedHttpClient.newCall(req).execute()) {
                    if (res.isSuccessful()) {
                        migrated++;
                        log.info("Document {} migré → storage_path {}", doc.getOssanGedDocumentId(), targetSpId);
                    } else {
                        log.warn("Échec migration doc {} : HTTP {}", doc.getOssanGedDocumentId(), res.code());
                        skipped++;
                    }
                }
            } catch (Exception e) {
                log.warn("Erreur migration doc {} : {}", doc.getOssanGedDocumentId(), e.getMessage());
                skipped++;
            }
        }
        String msg = String.format("Migration storage_path : %d migré(s), %d ignoré(s) sur %d documents", migrated, skipped, docs.size());
        log.info(msg);
        return DataResponse.success(msg);
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<DossierGedResponse>> arbreDossiers() {
        List<DossierGedResponse> list = dossierRepository.findAllSinistreDossiers().stream()
                .map(gedMapper::toDossierResponse)
                .collect(Collectors.toList());
        return DataResponse.success(list);
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<DocumentGedResponse>> listerDocumentsSinistre(UUID sinistreId) {
        List<DocumentGedResponse> list = documentRepository.findBySinistre(sinistreId).stream()
                .map(gedMapper::toDocumentResponse).collect(Collectors.toList());
        return DataResponse.success(list);
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<DocumentGedResponse>> listerDocumentsVictime(UUID victimeId) {
        List<DocumentGedResponse> list = documentRepository.findByVictime(victimeId).stream()
                .map(gedMapper::toDocumentResponse).collect(Collectors.toList());
        return DataResponse.success(list);
    }

    @Override
    public String verifierSessionPaperless(String cookieHeader) {
        if (cookieHeader == null || !cookieHeader.contains("sessionid=")) return null;
        try {
            // /api/profile/ renvoie le user courant si la session Django est valide
            Request req = new Request.Builder()
                    .url(ossanGedConfig.getBaseUrl() + "/api/profile/")
                    .header("Cookie", cookieHeader)
                    .header("Accept", "application/json")
                    .get().build();
            try (Response res = ossanGedHttpClient.newCall(req).execute()) {
                if (!res.isSuccessful()) return null;
                JsonNode node = objectMapper.readTree(res.body().string());
                String username = node.path("username").asText(null);
                if (username == null || username.isBlank()) return null;
                log.debug("Session Paperless valide : user={}", username);
                return username;
            }
        } catch (Exception e) {
            log.debug("Vérification session Paperless échouée : {}", e.getMessage());
            return null;
        }
    }

    @Override
    public void provisionnerUtilisateur(String username) {
        if (authToken == null || username == null || username.isBlank()) return;
        try {
            String searchUrl = ossanGedConfig.getBaseUrl() + "/api/users/?username="
                    + URLEncoder.encode(username, StandardCharsets.UTF_8);
            Request searchReq = new Request.Builder().url(searchUrl)
                    .header("Authorization", "Token " + authToken).get().build();
            try (Response searchRes = ossanGedHttpClient.newCall(searchReq).execute()) {
                if (!searchRes.isSuccessful()) {
                    log.warn("OssanGED /api/users/ inaccessible ({}), provision ignorée", searchRes.code());
                    return;
                }
                JsonNode root = objectMapper.readTree(searchRes.body().string());
                int count = root.path("count").asInt(0);
                if (count > 0) {
                    JsonNode existing = root.path("results").get(0);
                    if (existing.path("is_superuser").asBoolean(false)) return; // déjà superuser
                    int userId = existing.path("id").asInt();
                    String patchBody = "{\"is_superuser\":true,\"is_staff\":true}";
                    Request patchReq = new Request.Builder()
                            .url(ossanGedConfig.getBaseUrl() + "/api/users/" + userId + "/")
                            .header("Authorization", "Token " + authToken)
                            .method("PATCH", RequestBody.create(patchBody, MediaType.get("application/json")))
                            .build();
                    try (Response patchRes = ossanGedHttpClient.newCall(patchReq).execute()) {
                        if (patchRes.isSuccessful()) log.info("OssanGED : {} promu superuser (id={})", username, userId);
                        else log.warn("OssanGED : promotion {} échouée : {}", username, patchRes.code());
                    }
                } else {
                    String createBody = objectMapper.writeValueAsString(java.util.Map.of(
                            "username", username,
                            "password", UUID.randomUUID().toString(),
                            "is_superuser", true,
                            "is_staff", true));
                    Request createReq = new Request.Builder()
                            .url(ossanGedConfig.getBaseUrl() + "/api/users/")
                            .header("Authorization", "Token " + authToken)
                            .post(RequestBody.create(createBody, MediaType.get("application/json")))
                            .build();
                    try (Response createRes = ossanGedHttpClient.newCall(createReq).execute()) {
                        if (createRes.isSuccessful()) log.info("OssanGED : {} créé comme superuser", username);
                        else log.warn("OssanGED : création {} échouée : {}", username, createRes.code());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("OssanGED : provisionnement {} échoué : {}", username, e.getMessage());
        }
    }

    @Override
    @Transactional
    public DataResponse<DocumentGedResponse> resoudreDocument(UUID ossanGedDocumentTrackingId) {
        OssanGedDocument doc = documentRepository.findByOssanGedDocumentTrackingId(ossanGedDocumentTrackingId)
                .orElseThrow(() -> new RessourceNotFoundException("Document GED introuvable"));
        if (doc.getOssanGedDocumentId() != null) {
            return DataResponse.success("Document déjà résolu", gedMapper.toDocumentResponse(doc));
        }
        if (doc.getGedTaskId() == null) {
            return DataResponse.success("Aucune tâche GED en attente", gedMapper.toDocumentResponse(doc));
        }
        try {
            GedTaskStatus status = consulterStatutTache(doc.getGedTaskId());
            if (status.documentId() != null) {
                doc.setOssanGedDocumentId(status.documentId());
                doc.setGedTaskId(null);
                documentRepository.save(doc);
                log.info("Document GED résolu : tracking={} → ossanGedId={}", ossanGedDocumentTrackingId, status.documentId());
            } else {
                log.debug("Document GED encore en cours : tracking={}, statut={}", ossanGedDocumentTrackingId, status.status());
            }
        } catch (Exception e) {
            log.warn("Impossible de résoudre la tâche GED {} : {}", doc.getGedTaskId(), e.getMessage());
        }
        return DataResponse.success("Statut document GED", gedMapper.toDocumentResponse(doc));
    }

    // ── Get or create en cascade ──────────────────────────────────────────────

    private OssanGedDossier getOrCreateDossierSinistre(Sinistre sinistre, String loginAuteur) {
        return dossierRepository.findRootBySinistre(sinistre.getSinistreTrackingId()).orElseGet(() -> {
            int annee = sinistre.getDateAccident() != null ? sinistre.getDateAccident().getYear()
                    : sinistre.getDateDeclaration() != null ? sinistre.getDateDeclaration().getYear()
                    : LocalDate.now().getYear();
            String typeDir = TypeSinistre.SURVENU_TOGO == sinistre.getTypeSinistre() ? "ET" : "TE";
            String chemin = String.format("Sinistres/%d/%s/%s", annee, typeDir, sinistre.getNumeroSinistreLocal());
            Integer storagePathId = tryCreateStoragePath(chemin);
            OssanGedDossier d = OssanGedDossier.builder()
                    .ossanGedDossierTrackingId(UUID.randomUUID())
                    .ossanGedStoragePathId(storagePathId)
                    .cheminStockage(chemin)
                    .titre(sinistre.getNumeroSinistreLocal())
                    .typeDossier(TypeDossierOssanGed.SINISTRE)
                    .sinistre(sinistre)
                    .createdBy(loginAuteur).activeData(true).deletedData(false)
                    .fromTable(TypeTable.OSSAN_GED_DOSSIER).build();
            return dossierRepository.save(d);
        });
    }

    private OssanGedDossier getOrCreateDossierVictime(Victime victime, String loginAuteur) {
        return dossierRepository.findByVictime(victime.getVictimeTrackingId()).orElseGet(() -> {
            OssanGedDossier parent = victime.getSinistre() != null
                    ? getOrCreateDossierSinistre(victime.getSinistre(), loginAuteur)
                    : null;
            String nomVictime = victime.getNom().toUpperCase() + "_"
                    + victime.getPrenoms().replaceAll("\\s+", "_").toUpperCase();
            String chemin = (parent != null ? parent.getCheminStockage() : "Sinistres") + "/" + nomVictime;
            Integer correspondantId = tryCreateCorrespondent(nomVictime);
            if (correspondantId != null) {
                victime.setOssanGedCorrespondentId(correspondantId);
                victimeRepository.save(victime);
            }
            OssanGedDossier d = OssanGedDossier.builder()
                    .ossanGedDossierTrackingId(UUID.randomUUID())
                    .ossanGedCorrespondentId(correspondantId)
                    .cheminStockage(chemin)
                    .titre(nomVictime)
                    .typeDossier(TypeDossierOssanGed.VICTIME)
                    .sinistre(victime.getSinistre())
                    .victime(victime)
                    .parentDossier(parent)
                    .createdBy(loginAuteur).activeData(true).deletedData(false)
                    .fromTable(TypeTable.OSSAN_GED_DOSSIER).build();
            return dossierRepository.save(d);
        });
    }

    private OssanGedDossier getOrCreateDossierReclamation(DossierReclamation dr, String loginAuteur) {
        OssanGedDossier parent = getOrCreateDossierVictime(dr.getVictime(), loginAuteur);
        return dossierRepository.findByParentAndTypeAndTitre(parent.getHistoriqueId(),
                TypeDossierOssanGed.RECLAMATION, dr.getNumeroDossier())
                .orElseGet(() -> {
                    String chemin = parent.getCheminStockage() + "/" + dr.getNumeroDossier();
                    Integer storagePathId = tryCreateStoragePath(chemin);
                    OssanGedDossier d = OssanGedDossier.builder()
                            .ossanGedDossierTrackingId(UUID.randomUUID())
                            .ossanGedStoragePathId(storagePathId)
                            .cheminStockage(chemin)
                            .titre(dr.getNumeroDossier())
                            .typeDossier(TypeDossierOssanGed.RECLAMATION)
                            .sinistre(dr.getSinistre())
                            .victime(dr.getVictime())
                            .parentDossier(parent)
                            .createdBy(loginAuteur).activeData(true).deletedData(false)
                            .fromTable(TypeTable.OSSAN_GED_DOSSIER).build();
                    return dossierRepository.save(d);
                });
    }

    private DossierReclamation createDefaultDossierReclamation(Sinistre sinistre, Victime victime, String loginAuteur) {
        long count = dossierReclamationRepository.count() + 1;
        String numero = "REC-" + LocalDate.now().getYear() + "-" + String.format("%05d", count);
        DossierReclamation d = DossierReclamation.builder()
                .dossierTrackingId(UUID.randomUUID())
                .numeroDossier(numero)
                .dateOuverture(LocalDate.now())
                .statut(StatutDossierReclamation.OUVERT)
                .montantTotalReclame(BigDecimal.ZERO)
                .montantTotalRetenu(BigDecimal.ZERO)
                .sinistre(sinistre)
                .victime(victime)
                .createdBy(loginAuteur).activeData(true).deletedData(false)
                .fromTable(TypeTable.DOSSIER_RECLAMATION).build();
        return dossierReclamationRepository.save(d);
    }

    // ── Résolution des tags OssanGED ──────────────────────────────────────────

    private List<Integer> resolverTagsDocument(TypeDocumentOssanGed typeDoc, Sinistre sinistre,
            List<String> tagsExtra, List<Integer> tagIdsExplicites) {
        List<Integer> ids = new ArrayList<>();

        Integer enAttente = tagCache.get("EN_ATTENTE");
        if (enAttente != null)
            ids.add(enAttente);

        if (typeDoc != null) {
            Integer t = tagCache.get(typeDoc.name());
            if (t != null)
                ids.add(t);
        }

        if (sinistre != null) {
            String tag = TypeSinistre.SURVENU_TOGO == sinistre.getTypeSinistre() ? "SINISTRE_ET" : "SINISTRE_TE";
            Integer t = tagCache.get(tag);
            if (t != null)
                ids.add(t);
        }

        if (tagsExtra != null) {
            for (String name : tagsExtra) {
                Integer t = tagCache.get(name);
                if (t != null)
                    ids.add(t);
            }
        }

        if (tagIdsExplicites != null)
            ids.addAll(tagIdsExplicites);

        return ids.stream().distinct().collect(Collectors.toList());
    }

    // ── HTTP OssanGED ─────────────────────────────────────────────────────────

    private String obtainToken() throws IOException {
        String url = ossanGedConfig.getBaseUrl() + "/api/token/";
        String json = String.format("{\"username\":\"%s\",\"password\":\"%s\"}",
                ossanGedConfig.getUsername(), ossanGedConfig.getPassword());
        Request req = new Request.Builder().url(url)
                .post(RequestBody.create(json, MediaType.get("application/json"))).build();
        try (Response res = ossanGedHttpClient.newCall(req).execute()) {
            if (!res.isSuccessful())
                throw new IOException("OssanGED auth failed: " + res.code());
            return objectMapper.readTree(res.body().string()).get("token").asText();
        }
    }

    private void chargerCacheTags() throws IOException {
        if (authToken == null)
            return;
        String url = ossanGedConfig.getBaseUrl() + "/api/tags/?page_size=200";
        Request req = new Request.Builder().url(url)
                .header("Authorization", "Token " + authToken).get().build();
        try (Response res = ossanGedHttpClient.newCall(req).execute()) {
            if (!res.isSuccessful())
                return;
            JsonNode root = objectMapper.readTree(res.body().string());
            JsonNode results = root.get("results");
            if (results != null && results.isArray()) {
                tagCache.clear();
                results.forEach(t -> tagCache.put(t.get("name").asText(), t.get("id").asInt()));
            }
        }
    }

    private Integer tryCreateStoragePath(String path) {
        try {
            return createStoragePath(path);
        } catch (Exception e) {
            log.warn("OssanGED storage_path indispo ({}): {}", path, e.getMessage());
            return null;
        }
    }

    private Integer tryCreateCorrespondent(String name) {
        try {
            return createCorrespondent(name);
        } catch (Exception e) {
            log.warn("OssanGED correspondent indispo ({}): {}", name, e.getMessage());
            return null;
        }
    }

    private Integer createStoragePath(String path) throws IOException {
        String listUrl = ossanGedConfig.getBaseUrl() + "/api/storage_paths/";
        Integer existing = findByName(listUrl, path);
        if (existing != null)
            return existing;
        // {title} est un template Paperless : le fichier sera stocké à path/{title}.ext
        String body = objectMapper.writeValueAsString(Map.of(
                "name", path,
                "path", path + "/{title}"));
        return postJson("/api/storage_paths/", body);
    }

    private Integer createCorrespondent(String name) throws IOException {
        String listUrl = ossanGedConfig.getBaseUrl() + "/api/correspondents/";
        Integer existing = findByName(listUrl, name);
        if (existing != null)
            return existing;
        String body = objectMapper.writeValueAsString(Map.of("name", name));
        return postJson("/api/correspondents/", body);
    }

    /**
     * Recherche l'ID d'un objet Paperless par son nom exact.
     * Le paramètre ?name= de l'API Paperless n'est pas un filtre exact —
     * on charge tous les résultats et on filtre côté Java pour éviter les faux positifs.
     */
    private Integer findByName(String baseUrl, String exactName) throws IOException {
        String pageUrl = baseUrl + (baseUrl.contains("?") ? "&" : "?") + "page_size=500";
        Request req = new Request.Builder().url(pageUrl)
                .header("Authorization", "Token " + authToken).get().build();
        try (Response res = ossanGedHttpClient.newCall(req).execute()) {
            if (!res.isSuccessful()) return null;
            JsonNode root = objectMapper.readTree(res.body().string());
            JsonNode results = root.path("results");
            if (results.isArray()) {
                for (JsonNode item : results) {
                    if (exactName.equals(item.path("name").asText(null))) {
                        return item.path("id").asInt(0);
                    }
                }
            }
            return null;
        }
    }

    private Integer postJson(String path, String json) throws IOException {
        String url = ossanGedConfig.getBaseUrl() + path;
        Request req = new Request.Builder().url(url)
                .header("Authorization", "Token " + authToken)
                .post(RequestBody.create(json, MediaType.get("application/json"))).build();
        try (Response res = ossanGedHttpClient.newCall(req).execute()) {
            String body = res.body().string();
            if (!res.isSuccessful())
                throw new IOException("OssanGED POST " + path + " erreur " + res.code() + ": " + body);
            return objectMapper.readTree(body).get("id").asInt();
        }
    }

    private UploadLaunchResult uploadToOssanGed(MultipartFile file, String titre, Integer storagePathId,
            Integer correspondantId, List<Integer> tagIds) throws IOException {
        String url = ossanGedConfig.getBaseUrl() + "/api/documents/post_document/";
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("document", file.getOriginalFilename(),
                        RequestBody.create(file.getBytes(),
                                MediaType.get(file.getContentType() != null ? file.getContentType()
                                        : "application/octet-stream")))
                .addFormDataPart("title", titre);
        if (storagePathId != null)
            builder.addFormDataPart("storage_path", storagePathId.toString());
        if (correspondantId != null)
            builder.addFormDataPart("correspondent", correspondantId.toString());
        if (tagIds != null)
            tagIds.stream()
                    .filter(Objects::nonNull)
                    .forEach(t -> builder.addFormDataPart("tags", t.toString()));
        Request req = new Request.Builder().url(url)
                .header("Authorization", "Token " + authToken).post(builder.build()).build();
        try (Response res = ossanGedHttpClient.newCall(req).execute()) {
            String body = res.body().string();
            if (!res.isSuccessful())
                throw new IOException("OssanGED upload erreur " + res.code() + ": " + body);
            JsonNode node = objectMapper.readTree(body);
            Integer documentId = extractDocumentId(node, true);
            if (documentId != null) {
                return new UploadLaunchResult(documentId, null, "TERMINE", null);
            }

            String taskId = extractTaskId(node);
            if (taskId != null) {
                Integer consumedDocumentId = waitForDocumentId(taskId, uploadInlineWaitSeconds);
                if (consumedDocumentId != null) {
                    return new UploadLaunchResult(consumedDocumentId, taskId, "TERMINE", null);
                }
                return new UploadLaunchResult(null, taskId, "EN_COURS",
                        "OssanGED upload lancé, indexation OCR en cours");
            }

            throw new IOException("Réponse upload OssanGED inattendue: " + body);
        }
    }

    private Integer waitForDocumentId(String taskId, long waitSeconds) throws IOException {
        long attempts = Math.max(1, waitSeconds * 1000L / Math.max(250L, uploadTaskPollIntervalMillis));
        for (int attempt = 0; attempt < attempts; attempt++) {
            GedTaskStatus status = consulterStatutTache(taskId);
            if (status.documentId() != null) {
                return status.documentId();
            }
            if ("FAILURE".equalsIgnoreCase(status.status())) {
                return null;
            }
            try {
                Thread.sleep(uploadTaskPollIntervalMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Attente de la tâche OssanGED interrompue", e);
            }
        }
        return null;
    }



    @Override
    public GedTaskStatus consulterStatutTache(String taskId) {
        String url = ossanGedConfig.getBaseUrl() + "/api/tasks/?task_id="
                + URLEncoder.encode(taskId, StandardCharsets.UTF_8);
        Request req = new Request.Builder().url(url)
                .header("Authorization", "Token " + authToken)
                .get()
                .build();
        try (Response res = ossanGedHttpClient.newCall(req).execute()) {
            String body = res.body().string();
            if (!res.isSuccessful()) {
                throw new RuntimeException("OssanGED task lookup erreur " + res.code() + ": " + body);
            }
            JsonNode root = objectMapper.readTree(body);
            // /api/tasks/ retourne un tableau plat OU {count, results:[]}
            JsonNode tasks = root.isArray() ? root : root.path("results");
            if (tasks.isArray()) {
                for (JsonNode taskNode : tasks) {
                    String status = taskNode.path("status").asText(null);
                    Integer documentId = extractDocumentId(taskNode, false);
                    if (documentId != null) {
                        return new GedTaskStatus(taskId, status, documentId, taskNode.path("result").asText(null));
                    }
                    Integer duplicateDocumentId = extractDuplicateDocumentId(taskNode);
                    if (duplicateDocumentId != null) {
                        log.info("OssanGED doublon taskId={} → document existant id={}", taskId, duplicateDocumentId);
                        return new GedTaskStatus(taskId, "SUCCESS", duplicateDocumentId, taskNode.path("result").asText(null));
                    }
                    if (status != null) {
                        return new GedTaskStatus(taskId, status, null, taskNode.path("result").asText(null));
                    }
                }
            }
            return new GedTaskStatus(taskId, "PENDING", null, null);
        } catch (IOException e) {
            throw new RuntimeException("Consultation tâche OssanGED impossible : " + e.getMessage(), e);
        }
    }

    private static final Pattern RESULT_DOC_ID_PATTERN =
            Pattern.compile("(?:new document id|document id)\\s+(\\d+)", Pattern.CASE_INSENSITIVE);

    private Integer extractDocumentId(JsonNode node, boolean allowGenericIdField) {
        if (node == null || node.isMissingNode() || node.isNull()) return null;
        // Nœud entier direct
        if (node.isIntegralNumber()) return node.asInt();
        // Nœud texte : peut être un ID numérique ("4") ou un message de résultat
        if (node.isTextual()) {
            String text = node.asText().trim();
            // Cas 1 : related_document="4" → entier pur
            try { return Integer.parseInt(text); } catch (NumberFormatException ignored) {}
            // Cas 2 : result="Success. New document id 4 created"
            Matcher m = RESULT_DOC_ID_PATTERN.matcher(text);
            if (m.find()) return Integer.parseInt(m.group(1));
            return null;
        }
        if (node.isObject()) {
            List<String> fields = new ArrayList<>(List.of("document_id", "document", "related_document"));
            if (allowGenericIdField) fields.add("id");
            for (String field : fields) {
                Integer id = extractDocumentId(node.get(field), false);
                if (id != null) return id;
            }
            // Extraire depuis le champ "result" (message de succès Paperless)
            Integer fromResult = extractDocumentId(node.get("result"), false);
            if (fromResult != null) return fromResult;
            Integer fromData = extractDocumentId(node.get("data"), allowGenericIdField);
            if (fromData != null) return fromData;
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                Integer nested = extractDocumentId(child, allowGenericIdField);
                if (nested != null) return nested;
            }
        }
        return null;
    }

    private String extractTaskId(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isTextual()) {
            return node.asText();
        }
        if (node.isObject()) {
            for (String field : List.of("task_id", "task", "id")) {
                JsonNode child = node.get(field);
                if (child != null && child.isTextual()) {
                    return child.asText();
                }
            }
            for (String field : List.of("result", "data")) {
                String nested = extractTaskId(node.get(field));
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    private Integer extractDuplicateDocumentId(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isObject()) {
            JsonNode resultNode = node.get("result");
            if (resultNode != null && resultNode.isTextual()) {
                Matcher matcher = DUPLICATE_DOCUMENT_PATTERN.matcher(resultNode.asText());
                if (matcher.find()) {
                    return Integer.valueOf(matcher.group(1));
                }
            }
            JsonNode dataNode = node.get("data");
            if (dataNode != null) {
                Integer nested = extractDuplicateDocumentId(dataNode);
                if (nested != null) {
                    return nested;
                }
            }
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                Integer nested = extractDuplicateDocumentId(child);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    private byte[] downloadFromOssanGed(Integer documentId) throws IOException {
        String url = ossanGedConfig.getBaseUrl() + "/api/documents/" + documentId + "/download/";
        Request req = new Request.Builder().url(url)
                .header("Authorization", "Token " + authToken).get().build();
        try (Response res = ossanGedHttpClient.newCall(req).execute()) {
            if (!res.isSuccessful())
                throw new IOException("OssanGED download erreur " + res.code());
            return res.body().bytes();
        }
    }
}
