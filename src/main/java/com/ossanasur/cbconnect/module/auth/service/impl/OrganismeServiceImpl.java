package com.ossanasur.cbconnect.module.auth.service.impl;

import com.ossanasur.cbconnect.common.enums.TypeOrganisme;
import com.ossanasur.cbconnect.exception.AlreadyExistException;
import com.ossanasur.cbconnect.exception.BadRequestException;
import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.historique.OrganismeVersioningService;
import com.ossanasur.cbconnect.module.auth.dto.request.BrandingImageType;
import com.ossanasur.cbconnect.module.auth.dto.request.OrganismeRequest;
import com.ossanasur.cbconnect.module.auth.dto.response.OrganismeResponse;
import com.ossanasur.cbconnect.module.auth.entity.Organisme;
import com.ossanasur.cbconnect.module.auth.mapper.OrganismeMapper;
import com.ossanasur.cbconnect.module.auth.repository.OrganismeRepository;
import com.ossanasur.cbconnect.module.auth.service.OrganismeService;
import com.ossanasur.cbconnect.security.dto.response.TwoFactorStatusResponse;
import com.ossanasur.cbconnect.utils.DataResponse;
import com.ossanasur.cbconnect.utils.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class OrganismeServiceImpl implements OrganismeService {

    private final OrganismeRepository organismeRepository;
    private final OrganismeMapper organismeMapper;
    private final OrganismeVersioningService versioningService;

    @Value("${file.upload-dir:./uploads}")
    private String uploadBaseDir;

    private static final long MAX_BRANDING_SIZE_BYTES = 2L * 1024 * 1024;
    private static final Map<String, String> ALLOWED_MIME_TO_EXTENSION = Map.of(
            "image/png", "png",
            "image/jpeg", "jpg",
            "image/jpg", "jpg",
            "image/webp", "webp"
    );

    @Override
    @Transactional
    public DataResponse<OrganismeResponse> create(OrganismeRequest r, String loginAuteur) {
        if (organismeRepository.existsByCodeAndActiveDataTrueAndDeletedDataFalse(r.code()))
            throw new AlreadyExistException("Un organisme avec le code '" + r.code() + "' existe deja");
        if (organismeRepository.existsByEmailAndActiveDataTrueAndDeletedDataFalse(r.email()))
            throw new AlreadyExistException("Un organisme avec l'email '" + r.email() + "' existe deja");

        Organisme o = Organisme.builder()
                .organismeTrackingId(UUID.randomUUID())
                .typeOrganisme(r.typeOrganisme()).raisonSociale(r.raisonSociale())
                .code(r.code()).email(r.email()).responsable(r.responsable())
                .contacts(r.contacts()).codePays(r.codePays()).codePaysBCB(r.codePaysBCB())
                .paysId(r.paysId()).dateCreation(r.dateCreation())
                .numeroAgrement(r.numeroAgrement()).apiEndpointUrl(r.apiEndpointUrl())
                .adresse(r.adresse()).boitePostale(r.boitePostale()).ville(r.ville())
                .telephonePrincipal(r.telephonePrincipal()).fax(r.fax()).siteWeb(r.siteWeb())
                .logo(r.logo())
                .headerImageUrl(r.headerImageUrl()).footerImageUrl(r.footerImageUrl())
                .titreResponsable(r.titreResponsable())
                .afficherDeuxSignatures(r.afficherDeuxSignatures())
                .responsable2(r.responsable2()).titreResponsable2(r.titreResponsable2())
                .active(r.active()).createdBy(loginAuteur).activeData(true).deletedData(false)
                .fromTable(com.ossanasur.cbconnect.common.enums.TypeTable.ORGANISME)
                .build();
        return DataResponse.created("Organisme cree avec succes", organismeMapper.toResponse(organismeRepository.save(o)));
    }

    @Override
    @Transactional
    public DataResponse<OrganismeResponse> update(UUID id, OrganismeRequest r, String loginAuteur) {
        Organisme updated = versioningService.createVersion(id, r, loginAuteur);
        return DataResponse.success("Organisme mis a jour avec succes", organismeMapper.toResponse(updated));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<OrganismeResponse> getByTrackingId(UUID id) {
        Organisme o = versioningService.getActiveVersion(id);
        return DataResponse.success(organismeMapper.toResponse(o));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<OrganismeResponse>> getAll() {
        List<OrganismeResponse> list = organismeRepository.findAllActive()
                .stream().map(organismeMapper::toResponse).collect(Collectors.toList());
        return DataResponse.success(list);
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<OrganismeResponse>> getAllByType(TypeOrganisme type) {
        List<OrganismeResponse> list = organismeRepository.findAllActiveByType(type)
                .stream().map(organismeMapper::toResponse).collect(Collectors.toList());
        return DataResponse.success(list);
    }

    @Override
    @Transactional
    public DataResponse<Void> delete(UUID id, String loginAuteur) {
        versioningService.softDelete(id, loginAuteur);
        return DataResponse.success("Organisme supprime avec succes", null);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<OrganismeResponse> getHistory(UUID id, int page, int size) {
        Page<OrganismeResponse> history = organismeRepository
                .findHistoryByTrackingId(id, PageRequest.of(page, size))
                .map(organismeMapper::toResponse);
        return PaginatedResponse.fromPage(history, "Historique organisme");
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<TwoFactorStatusResponse> getTwoFactor(UUID trackingId) {
        Organisme o = versioningService.getActiveVersion(trackingId);
        return DataResponse.success(new TwoFactorStatusResponse(o.isTwoFactorEnabled()));
    }

    @Override
    @Transactional
    public DataResponse<TwoFactorStatusResponse> updateTwoFactor(UUID trackingId, boolean enabled, String loginAuteur) {
        Organisme o = versioningService.getActiveVersion(trackingId);
        o.setTwoFactorEnabled(enabled);
        o.setUpdatedBy(loginAuteur);
        o.setUpdatedAt(java.time.LocalDateTime.now());
        organismeRepository.save(o);
        String msg = enabled
                ? "Double authentification activée pour l'organisme"
                : "Double authentification désactivée pour l'organisme";
        return DataResponse.success(msg, new TwoFactorStatusResponse(enabled));
    }

    @Override
    @Transactional
    public DataResponse<OrganismeResponse> uploadBrandingImage(UUID trackingId, BrandingImageType type,
                                                                MultipartFile file, String loginAuteur) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Le fichier est vide");
        }
        if (file.getSize() > MAX_BRANDING_SIZE_BYTES) {
            throw new BadRequestException("Image trop volumineuse (max 2 Mo)");
        }
        String contentType = file.getContentType() != null ? file.getContentType().toLowerCase(Locale.ROOT) : "";
        String extension = ALLOWED_MIME_TO_EXTENSION.get(contentType);
        if (extension == null) {
            throw new BadRequestException("Format non supporté — formats acceptés : PNG, JPEG, WEBP");
        }

        Organisme o = versioningService.getActiveVersion(trackingId);

        Path dir = Paths.get(uploadBaseDir, "organismes", trackingId.toString());
        try {
            Files.createDirectories(dir);
            deleteExistingBrandingFiles(dir, type.getSlug());
            Path target = dir.resolve(type.getSlug() + "." + extension);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException("Stockage de l'image branding échoué : " + e.getMessage(), e);
        }

        String url = "/v1/organismes/" + trackingId + "/branding/" + type.getSlug();
        switch (type) {
            case LOGO -> o.setLogo(url);
            case HEADER -> o.setHeaderImageUrl(url);
            case FOOTER -> o.setFooterImageUrl(url);
        }
        o.setUpdatedBy(loginAuteur);
        o.setUpdatedAt(LocalDateTime.now());
        Organisme saved = organismeRepository.save(o);
        return DataResponse.success("Image branding mise à jour", organismeMapper.toResponse(saved));
    }

    @Override
    @Transactional
    public DataResponse<OrganismeResponse> deleteBrandingImage(UUID trackingId, BrandingImageType type, String loginAuteur) {
        Organisme o = versioningService.getActiveVersion(trackingId);
        Path dir = Paths.get(uploadBaseDir, "organismes", trackingId.toString());
        deleteExistingBrandingFiles(dir, type.getSlug());

        switch (type) {
            case LOGO -> o.setLogo(null);
            case HEADER -> o.setHeaderImageUrl(null);
            case FOOTER -> o.setFooterImageUrl(null);
        }
        o.setUpdatedBy(loginAuteur);
        o.setUpdatedAt(LocalDateTime.now());
        Organisme saved = organismeRepository.save(o);
        return DataResponse.success("Image branding supprimée", organismeMapper.toResponse(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadBrandingImage(UUID trackingId, BrandingImageType type) {
        Path dir = Paths.get(uploadBaseDir, "organismes", trackingId.toString());
        Optional<Path> found = findBrandingFile(dir, type.getSlug());
        if (found.isEmpty()) {
            throw new RessourceNotFoundException("Image branding introuvable");
        }
        Path file = found.get();
        Resource res = new FileSystemResource(file);
        String mime = guessMimeFromFilename(file.getFileName().toString());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mime))
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=300")
                .body(res);
    }

    private void deleteExistingBrandingFiles(Path dir, String slug) {
        if (!Files.isDirectory(dir)) return;
        try (Stream<Path> entries = Files.list(dir)) {
            entries.filter(p -> {
                String name = p.getFileName().toString();
                int dot = name.lastIndexOf('.');
                String base = dot > 0 ? name.substring(0, dot) : name;
                return base.equalsIgnoreCase(slug);
            }).forEach(p -> {
                try { Files.deleteIfExists(p); } catch (IOException ignored) {}
            });
        } catch (IOException ignored) {}
    }

    private Optional<Path> findBrandingFile(Path dir, String slug) {
        if (!Files.isDirectory(dir)) return Optional.empty();
        try (Stream<Path> entries = Files.list(dir)) {
            return entries.filter(p -> {
                String name = p.getFileName().toString();
                int dot = name.lastIndexOf('.');
                String base = dot > 0 ? name.substring(0, dot) : name;
                return base.equalsIgnoreCase(slug);
            }).findFirst();
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private String guessMimeFromFilename(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".webp")) return "image/webp";
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
}
