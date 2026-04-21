package com.ossanasur.cbconnect.module.ged.controller;

import com.ossanasur.cbconnect.module.ged.dto.request.UploadDocumentRequest;
import com.ossanasur.cbconnect.module.ged.dto.response.DocumentGedResponse;
import com.ossanasur.cbconnect.module.ged.dto.response.DossierGedResponse;
import com.ossanasur.cbconnect.module.ged.service.OssanGedClientService;
import com.ossanasur.cbconnect.utils.DataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/ged")
@RequiredArgsConstructor
@Tag(name = "OssanGED by OSSANASUR", description = "Proxy Spring Boot vers OssanGED (le frontend ne communique jamais directement avec OssanGED)")
@SecurityRequirement(name = "bearerAuth")
public class GedController {
    private final OssanGedClientService gedService;

    @PostMapping("/dossiers/sinistre/{sinistreId}")
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR','SECRETAIRE')")
    @Operation(summary = "Creer le dossier GED d'un sinistre dans OssanGed")
    public ResponseEntity<DataResponse<DossierGedResponse>> creerDossierSinistre(
            @PathVariable UUID sinistreId, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(gedService.creerDossierSinistre(sinistreId, u.getUsername()));
    }

    @PostMapping("/dossiers/victime/{victimeId}")
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR')")
    @Operation(summary = "Creer le sous-dossier GED d'une victime dans OssanGed")
    public ResponseEntity<DataResponse<DossierGedResponse>> creerDossierVictime(
            @PathVariable UUID victimeId, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(gedService.creerDossierVictime(victimeId, u.getUsername()));
    }

    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR','SECRETAIRE')")
    @Operation(summary = "Uploader un document vers OssanGed via le proxy")
    public ResponseEntity<DataResponse<DocumentGedResponse>> upload(
            @RequestPart("file") MultipartFile file,
            @RequestPart("metadata") UploadDocumentRequest r,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(gedService.uploadDocument(file, r, u.getUsername()));
    }

    @GetMapping("/documents/{ossanGedDocumentId}/telecharger")
    @Operation(summary = "Telecharger un document depuis OssanGed")
    public ResponseEntity<byte[]> telecharger(@PathVariable Integer ossanGedDocumentId) {
        byte[] data = gedService.telechargerDocument(ossanGedDocumentId).getData();
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(data);
    }

    @GetMapping("/sinistres/{sinistreId}/documents")
    @Operation(summary = "Lister tous les documents d'un sinistre")
    public ResponseEntity<DataResponse<List<DocumentGedResponse>>> docsSinistre(@PathVariable UUID sinistreId) {
        return ResponseEntity.ok(gedService.listerDocumentsSinistre(sinistreId));
    }

    @GetMapping("/victimes/{victimeId}/documents")
    @Operation(summary = "Lister tous les documents d'une victime")
    public ResponseEntity<DataResponse<List<DocumentGedResponse>>> docsVictime(@PathVariable UUID victimeId) {
        return ResponseEntity.ok(gedService.listerDocumentsVictime(victimeId));
    }
}
