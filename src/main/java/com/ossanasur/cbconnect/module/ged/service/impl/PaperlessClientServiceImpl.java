package com.ossanasur.cbconnect.module.ged.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ossanasur.cbconnect.common.enums.TypeDossierOssanGed;
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
import com.ossanasur.cbconnect.module.ged.repository.OssanGedDossierRepository;
import com.ossanasur.cbconnect.module.ged.service.PaperlessClientService;
import com.ossanasur.cbconnect.module.sinistre.repository.SinistreRepository;
import com.ossanasur.cbconnect.module.sinistre.repository.VictimeRepository;
import com.ossanasur.cbconnect.utils.DataResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * Proxy Spring Boot vers l'API REST Paperless-ngx.
 * Le React frontend ne communique JAMAIS directement avec Paperless.
 * Tout transite par ces endpoints /v1/ged/**.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaperlessClientServiceImpl implements PaperlessClientService {

    private final OssanGedConfig paperlessConfig;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final OssanGedDossierRepository dossierRepository;
    private final SinistreRepository sinistreRepository;
    private final VictimeRepository victimeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final GedMapper gedMapper;

    private String authToken; // Token Paperless conservé côté serveur uniquement

    /** Récupère ou rafraîchit le token Paperless au démarrage */
    @PostConstruct
    public void initToken() {
        try {
            authToken = obtainPaperlessToken();
            log.info("Token Paperless-ngx obtenu avec succès");
        } catch (Exception e) {
            log.warn("Paperless-ngx indisponible au démarrage : {}", e.getMessage());
        }
    }

    /** Initialise les tags Paperless (idempotent, appelé au setup) */
    @Override
    @PostConstruct
    public void initTagsPaperless() {
        if (authToken == null)
            return;
        String[] tags = { "PV", "CMI", "CMC", "EXPERTISE_MED", "EXPERTISE_AUTO", "CMI", "FACTURE_MED",
                "FACTURE_RECLAMATION", "ORDONNANCE", "COURRIER", "ACTE_DECES", "SCOLARITE" };
        for (String tag : tags) {
            try {
                createTagIfAbsent(tag);
            } catch (Exception e) {
                log.debug("Tag {} peut-etre existant : {}", tag, e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public DataResponse<DossierGedResponse> creerDossierSinistre(UUID sinistreId, String loginAuteur) {
        var sinistre = sinistreRepository.findActiveByTrackingId(sinistreId)
                .orElseThrow(() -> new RessourceNotFoundException("Sinistre introuvable"));

        // Vérifier si dossier existe déjà
        Optional<OssanGedDossier> existing = dossierRepository.findRootBySinistre(sinistreId);
        if (existing.isPresent())
            return DataResponse.success("Dossier GED existant", gedMapper.toDossierResponse(existing.get()));

        String chemin = "BNCB/Sinistres/" + sinistre.getNumeroSinistreLocal();
        Integer storagePathId = null;
        try {
            storagePathId = createStoragePath(chemin);
        } catch (Exception e) {
            log.warn("Paperless indisponible, creation dossier local seulement : {}", e.getMessage());
        }

        OssanGedDossier dossier = OssanGedDossier.builder()
                .ossanGedDossierTrackingId(UUID.randomUUID())
                .ossanGedStoragePathId(storagePathId)
                .cheminStockage(chemin).titre(sinistre.getNumeroSinistreLocal())
                .typeDossier(TypeDossierOssanGed.SINISTRE).sinistre(sinistre)
                .createdBy(loginAuteur).activeData(true).deletedData(false)
                .fromTable(TypeTable.OSSAN_GED_DOSSIER).build();
        return DataResponse.created("Dossier GED cree", gedMapper.toDossierResponse(dossierRepository.save(dossier)));
    }

    @Override
    @Transactional
    public DataResponse<DossierGedResponse> creerDossierVictime(UUID victimeId, String loginAuteur) {
        var victime = victimeRepository.findActiveByTrackingId(victimeId)
                .orElseThrow(() -> new RessourceNotFoundException("Victime introuvable"));

        Optional<OssanGedDossier> existing = dossierRepository.findByVictime(victimeId);
        if (existing.isPresent())
            return DataResponse.success("Dossier GED existant", gedMapper.toDossierResponse(existing.get()));

        // Trouver le dossier parent sinistre
        OssanGedDossier parentDossier = null;
        if (victime.getSinistre() != null) {
            parentDossier = dossierRepository.findRootBySinistre(victime.getSinistre().getSinistreTrackingId())
                    .orElse(null);
        }

        String nomVictime = victime.getNom().toUpperCase() + "-" + victime.getPrenoms().toUpperCase().replace(" ", "-");
        String chemin = (parentDossier != null ? parentDossier.getCheminStockage() : "BNCB/Sinistres") + "/"
                + nomVictime;

        Integer correspondentId = null;
        try {
            correspondentId = createCorrespondent(nomVictime);
        } catch (Exception e) {
            log.warn("Impossible de créer le correspondent Paperless : {}", e.getMessage());
        }

        OssanGedDossier dossier = OssanGedDossier.builder()
                .ossanGedDossierTrackingId(UUID.randomUUID())
                .ossanGedCorrespondentId(correspondentId)
                .cheminStockage(chemin).titre(nomVictime)
                .typeDossier(TypeDossierOssanGed.VICTIME)
                .sinistre(victime.getSinistre()).victime(victime)
                .parentDossier(parentDossier)
                .createdBy(loginAuteur).activeData(true).deletedData(false)
                .fromTable(TypeTable.OSSAN_GED_DOSSIER).build();

        // Mettre à jour l'ID paperless sur la victime
        victime.setPaperlessCorrespondentId(correspondentId);
        victimeRepository.save(victime);
        return DataResponse.created("Dossier victime cree",
                gedMapper.toDossierResponse(dossierRepository.save(dossier)));
    }

    @Override
    @Transactional
    public DataResponse<DocumentGedResponse> uploadDocument(MultipartFile file, UploadDocumentRequest r,
            String loginAuteur) {
        if (file.isEmpty())
            throw new BadRequestException("Fichier vide");
        OssanGedDossier dossier = null;
        if (r.victimeTrackingId() != null) {
            dossier = dossierRepository.findByVictime(r.victimeTrackingId()).orElse(null);
        }
        if (dossier == null && r.sinistreTrackingId() != null) {
            dossier = dossierRepository.findRootBySinistre(r.sinistreTrackingId()).orElse(null);
        }
        if (dossier == null)
            throw new RessourceNotFoundException("Dossier GED introuvable, creer le dossier d abord");

        Integer paperlessId = null;
        // String checksum = null;
        try {
            paperlessId = uploadToPaperless(file, r.titre(), dossier.getOssanGedStoragePathId(), r.tagIds());
        } catch (Exception e) {
            log.warn("Upload Paperless echoue, metadata locale uniquement : {}", e.getMessage());
        }

        final OssanGedDossier finalDossier = dossier;
        var sinistre = r.sinistreTrackingId() != null
                ? sinistreRepository.findActiveByTrackingId(r.sinistreTrackingId()).orElse(null)
                : null;
        var victime = r.victimeTrackingId() != null
                ? victimeRepository.findActiveByTrackingId(r.victimeTrackingId()).orElse(null)
                : null;
        var uploader = utilisateurRepository.findByEmailAndActiveDataTrueAndDeletedDataFalse(loginAuteur).orElse(null);

        OssanGedDocument doc = OssanGedDocument.builder()
                .ossanGedDocumentTrackingId(UUID.randomUUID())
                .ossanGedDocumentId(paperlessId != null ? paperlessId : (int) (System.currentTimeMillis() % 100000))
                .titre(r.titre()).typeDocument(r.typeDocument()).dateDocument(r.dateDocument())
                .mimeType(file.getContentType()).dossier(finalDossier)
                .sinistre(sinistre).victime(victime).uploadePar(uploader)
                .createdBy(loginAuteur).activeData(true).deletedData(false)
                .fromTable(TypeTable.OSSAN_GED_DOCUMENT).build();
        // Note: PaperlessDocumentRepository injection needed here
        return DataResponse.created("Document uploade", gedMapper.toDocumentResponse(doc));
    }

    @Override
    public DataResponse<byte[]> telechargerDocument(Integer paperlessDocumentId) {
        try {
            byte[] data = downloadFromPaperless(paperlessDocumentId);
            return DataResponse.success(data);
        } catch (Exception e) {
            throw new RuntimeException("Impossible de télécharger le document Paperless : " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<DocumentGedResponse>> listerDocumentsSinistre(UUID sinistreId) {
        // TODO: query documents by sinistre via PaperlessDocumentRepository
        return DataResponse.success(Collections.emptyList());
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<DocumentGedResponse>> listerDocumentsVictime(UUID victimeId) {
        return DataResponse.success(Collections.emptyList());
    }

    // ── Méthodes privées HTTP vers Paperless-ngx ──────────────────────────────

    private String obtainPaperlessToken() throws IOException {
        String url = paperlessConfig.getBaseUrl() + "/api/token/";
        String json = "{\"username\":\"" + paperlessConfig.getUsername() + "\",\"password\":\""
                + paperlessConfig.getPassword() + "\"}";
        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
        Request request = new Request.Builder().url(url).post(body).build();
        try (Response response = httpClient.newCall(request).execute()) {
            JsonNode node = objectMapper.readTree(response.body().string());
            return node.get("token").asText();
        }
    }

    private Integer createStoragePath(String path) throws IOException {
        String url = paperlessConfig.getBaseUrl() + "/api/storage_paths/";
        String json = "{\"path\":\"" + path + "\",\"name\":\"" + path + "\"}";
        return callPaperlessPost(url, json, "id");
    }

    private Integer createCorrespondent(String name) throws IOException {
        String url = paperlessConfig.getBaseUrl() + "/api/correspondents/";
        String json = "{\"name\":\"" + name + "\"}";
        return callPaperlessPost(url, json, "id");
    }

    private void createTagIfAbsent(String name) throws IOException {
        String url = paperlessConfig.getBaseUrl() + "/api/tags/";
        String json = "{\"name\":\"" + name + "\",\"color\":\"#2E75B6\"}";
        callPaperlessPost(url, json, "id");
    }

    private Integer uploadToPaperless(MultipartFile file, String title, Integer storagePathId, List<Integer> tagIds)
            throws IOException {
        String url = paperlessConfig.getBaseUrl() + "/api/documents/post_document/";
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("document", file.getOriginalFilename(),
                        RequestBody.create(file.getBytes(),
                                MediaType.get(file.getContentType() != null ? file.getContentType()
                                        : "application/octet-stream")))
                .addFormDataPart("title", title);
        if (storagePathId != null)
            builder.addFormDataPart("storage_path", storagePathId.toString());
        if (tagIds != null)
            tagIds.forEach(t -> builder.addFormDataPart("tags", t.toString()));
        Request request = new Request.Builder().url(url)
                .header("Authorization", "Token " + authToken).post(builder.build()).build();
        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (!response.isSuccessful())
                throw new IOException("Paperless upload error " + response.code() + ": " + responseBody);
            return objectMapper.readTree(responseBody).get("id").asInt();
        }
    }

    private byte[] downloadFromPaperless(Integer documentId) throws IOException {
        String url = paperlessConfig.getBaseUrl() + "/api/documents/" + documentId + "/download/";
        Request request = new Request.Builder().url(url).header("Authorization", "Token " + authToken).get().build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new IOException("Download failed: " + response.code());
            return response.body().bytes();
        }
    }

    private Integer callPaperlessPost(String url, String json, String idField) throws IOException {
        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
        Request request = new Request.Builder().url(url)
                .header("Authorization", "Token " + authToken).post(body).build();
        try (Response response = httpClient.newCall(request).execute()) {
            return objectMapper.readTree(response.body().string()).get(idField).asInt();
        }
    }
}
