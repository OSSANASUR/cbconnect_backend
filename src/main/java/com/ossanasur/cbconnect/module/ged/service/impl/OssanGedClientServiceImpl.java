package com.ossanasur.cbconnect.module.ged.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ossanasur.cbconnect.common.enums.StatutDossierReclamation;
import com.ossanasur.cbconnect.common.enums.TypeDocumentOssanGed;
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
import com.ossanasur.cbconnect.module.ged.repository.OssanGedDocumentRepository;
import com.ossanasur.cbconnect.module.ged.repository.OssanGedDossierRepository;
import com.ossanasur.cbconnect.module.ged.service.OssanGedClientService;
import com.ossanasur.cbconnect.module.reclamation.entity.DossierReclamation;
import com.ossanasur.cbconnect.module.reclamation.repository.DossierReclamationRepository;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import com.ossanasur.cbconnect.module.sinistre.entity.Victime;
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
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Proxy Spring Boot vers OssanGED (moteur GED reconfiguré).
 * Le frontend ne communique JAMAIS directement avec OssanGED.
 *
 * Hiérarchie des dossiers :
 *   Sinistres/<ET|TE>/<NumeroSinistre>/<NOM_PRENOMS_Victime>/<NumeroDossierReclamation>/
 *
 * Création en cascade automatique à l'upload si un niveau manque.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OssanGedClientServiceImpl implements OssanGedClientService {

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
    public DataResponse<DocumentGedResponse> uploadDocument(MultipartFile file, UploadDocumentRequest r, String loginAuteur) {
        if (file.isEmpty()) throw new BadRequestException("Fichier vide");
        if (r.sinistreTrackingId() == null) throw new BadRequestException("sinistreTrackingId requis");

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

        Integer ossanGedDocId = null;
        try {
            ossanGedDocId = uploadToOssanGed(file, r.titre(),
                dossier.getOssanGedStoragePathId(),
                dossier.getOssanGedCorrespondentId(),
                tagIds);
            log.info("Document uploade dans OssanGED : id={}, titre={}", ossanGedDocId, r.titre());
        } catch (Exception e) {
            log.warn("Upload OssanGED echoue, metadata locale uniquement : {}", e.getMessage());
        }

        var uploader = utilisateurRepository.findByEmailAndActiveDataTrueAndDeletedDataFalse(loginAuteur).orElse(null);

        OssanGedDocument doc = OssanGedDocument.builder()
            .ossanGedDocumentTrackingId(UUID.randomUUID())
            .ossanGedDocumentId(ossanGedDocId != null ? ossanGedDocId : (int) (System.currentTimeMillis() % 1_000_000))
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

        return DataResponse.created("Document indexe dans OssanGED",
            gedMapper.toDocumentResponse(documentRepository.save(doc)));
    }

    @Override
    public DataResponse<byte[]> telechargerDocument(Integer ossanGedDocumentId) {
        try {
            return DataResponse.success(downloadFromOssanGed(ossanGedDocumentId));
        } catch (Exception e) {
            throw new RuntimeException("Impossible de telecharger depuis OssanGED : " + e.getMessage());
        }
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

    // ── Get or create en cascade ──────────────────────────────────────────────

    private OssanGedDossier getOrCreateDossierSinistre(Sinistre sinistre, String loginAuteur) {
        return dossierRepository.findRootBySinistre(sinistre.getSinistreTrackingId()).orElseGet(() -> {
            String typeDir = "SURVENU_TOGO".equals(sinistre.getTypeSinistre().name()) ? "ET" : "TE";
            String chemin = String.format("Sinistres/%s/%s", typeDir, sinistre.getNumeroSinistreLocal());
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
                ? getOrCreateDossierSinistre(victime.getSinistre(), loginAuteur) : null;
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
        if (enAttente != null) ids.add(enAttente);

        if (typeDoc != null) {
            Integer t = tagCache.get(typeDoc.name());
            if (t != null) ids.add(t);
        }

        if (sinistre != null) {
            String tag = "SURVENU_TOGO".equals(sinistre.getTypeSinistre().name()) ? "SINISTRE_ET" : "SINISTRE_TE";
            Integer t = tagCache.get(tag);
            if (t != null) ids.add(t);
        }

        if (tagsExtra != null) {
            for (String name : tagsExtra) {
                Integer t = tagCache.get(name);
                if (t != null) ids.add(t);
            }
        }

        if (tagIdsExplicites != null) ids.addAll(tagIdsExplicites);

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
            if (!res.isSuccessful()) throw new IOException("OssanGED auth failed: " + res.code());
            return objectMapper.readTree(res.body().string()).get("token").asText();
        }
    }

    private void chargerCacheTags() throws IOException {
        if (authToken == null) return;
        String url = ossanGedConfig.getBaseUrl() + "/api/tags/?page_size=200";
        Request req = new Request.Builder().url(url)
            .header("Authorization", "Token " + authToken).get().build();
        try (Response res = ossanGedHttpClient.newCall(req).execute()) {
            if (!res.isSuccessful()) return;
            JsonNode root = objectMapper.readTree(res.body().string());
            JsonNode results = root.get("results");
            if (results != null && results.isArray()) {
                tagCache.clear();
                results.forEach(t -> tagCache.put(t.get("name").asText(), t.get("id").asInt()));
            }
        }
    }

    private Integer tryCreateStoragePath(String path) {
        try { return createStoragePath(path); }
        catch (Exception e) { log.warn("OssanGED storage_path indispo ({}): {}", path, e.getMessage()); return null; }
    }

    private Integer tryCreateCorrespondent(String name) {
        try { return createCorrespondent(name); }
        catch (Exception e) { log.warn("OssanGED correspondent indispo ({}): {}", name, e.getMessage()); return null; }
    }

    private Integer createStoragePath(String path) throws IOException {
        String search = ossanGedConfig.getBaseUrl() + "/api/storage_paths/?name="
            + URLEncoder.encode(path, StandardCharsets.UTF_8);
        Integer existing = findByName(search);
        if (existing != null) return existing;
        String json = String.format("{\"name\":\"%s\",\"path\":\"%s\"}", path, path);
        return postJson("/api/storage_paths/", json);
    }

    private Integer createCorrespondent(String name) throws IOException {
        String search = ossanGedConfig.getBaseUrl() + "/api/correspondents/?name="
            + URLEncoder.encode(name, StandardCharsets.UTF_8);
        Integer existing = findByName(search);
        if (existing != null) return existing;
        String json = String.format("{\"name\":\"%s\"}", name);
        return postJson("/api/correspondents/", json);
    }

    private Integer findByName(String url) throws IOException {
        Request req = new Request.Builder().url(url)
            .header("Authorization", "Token " + authToken).get().build();
        try (Response res = ossanGedHttpClient.newCall(req).execute()) {
            if (!res.isSuccessful()) return null;
            JsonNode root = objectMapper.readTree(res.body().string());
            if (root.get("count").asInt() > 0) return root.get("results").get(0).get("id").asInt();
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
            if (!res.isSuccessful()) throw new IOException("OssanGED POST " + path + " erreur " + res.code() + ": " + body);
            return objectMapper.readTree(body).get("id").asInt();
        }
    }

    private Integer uploadToOssanGed(MultipartFile file, String titre, Integer storagePathId,
                                     Integer correspondantId, List<Integer> tagIds) throws IOException {
        String url = ossanGedConfig.getBaseUrl() + "/api/documents/post_document/";
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("document", file.getOriginalFilename(),
                RequestBody.create(file.getBytes(),
                    MediaType.get(file.getContentType() != null ? file.getContentType() : "application/octet-stream")))
            .addFormDataPart("title", titre);
        if (storagePathId != null)  builder.addFormDataPart("storage_path", storagePathId.toString());
        if (correspondantId != null) builder.addFormDataPart("correspondent", correspondantId.toString());
        if (tagIds != null) tagIds.forEach(t -> builder.addFormDataPart("tags", t.toString()));
        Request req = new Request.Builder().url(url)
            .header("Authorization", "Token " + authToken).post(builder.build()).build();
        try (Response res = ossanGedHttpClient.newCall(req).execute()) {
            String body = res.body().string();
            if (!res.isSuccessful()) throw new IOException("OssanGED upload erreur " + res.code() + ": " + body);
            JsonNode node = objectMapper.readTree(body);
            return node.isInt() ? node.asInt() : (node.get("id") != null ? node.get("id").asInt() : null);
        }
    }

    private byte[] downloadFromOssanGed(Integer documentId) throws IOException {
        String url = ossanGedConfig.getBaseUrl() + "/api/documents/" + documentId + "/download/";
        Request req = new Request.Builder().url(url)
            .header("Authorization", "Token " + authToken).get().build();
        try (Response res = ossanGedHttpClient.newCall(req).execute()) {
            if (!res.isSuccessful()) throw new IOException("OssanGED download erreur " + res.code());
            return res.body().bytes();
        }
    }
}
