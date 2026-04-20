package com.ossanasur.cbconnect.module.auth.controller;

import com.ossanasur.cbconnect.module.auth.dto.request.HabilitationRequest;
import com.ossanasur.cbconnect.module.auth.dto.response.HabilitationResponse;
import com.ossanasur.cbconnect.module.auth.service.HabilitationService;
import com.ossanasur.cbconnect.module.auth.service.HabilitationImportService;
import com.ossanasur.cbconnect.utils.DataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/habilitations")
@RequiredArgsConstructor
@Tag(name = "Habilitations", description = "CRUD des habilitations RBAC")
@SecurityRequirement(name = "bearerAuth")
public class HabilitationController {

    private final HabilitationService habilitationService;
    private final HabilitationImportService habilitationImportService;

    @PostMapping
    @Operation(summary = "Creer une habilitation")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SE')")
    public ResponseEntity<DataResponse<HabilitationResponse>> create(
            @Valid @RequestBody HabilitationRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(habilitationService.create(request, user.getUsername()));
    }

    @GetMapping
    @Operation(summary = "Lister toutes les habilitations actives")
    public ResponseEntity<DataResponse<List<HabilitationResponse>>> getAll() {
        return ResponseEntity.ok(habilitationService.getAll());
    }

    @GetMapping("/module/{moduleTrackingId}")
    @Operation(summary = "Lister les habilitations d'un module")
    public ResponseEntity<DataResponse<List<HabilitationResponse>>> getByModule(
            @PathVariable UUID moduleTrackingId) {
        return ResponseEntity.ok(habilitationService.getByModule(moduleTrackingId));
    }

    @GetMapping("/{trackingId}")
    @Operation(summary = "Detail d'une habilitation")
    public ResponseEntity<DataResponse<HabilitationResponse>> getOne(@PathVariable UUID trackingId) {
        return ResponseEntity.ok(habilitationService.getByTrackingId(trackingId));
    }

    @PutMapping("/{trackingId}")
    @Operation(summary = "Modifier une habilitation")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SE')")
    public ResponseEntity<DataResponse<HabilitationResponse>> update(
            @PathVariable UUID trackingId,
            @Valid @RequestBody HabilitationRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(habilitationService.update(trackingId, request, user.getUsername()));
    }

    @DeleteMapping("/{trackingId}")
    @Operation(summary = "Supprimer logiquement une habilitation")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SE')")
    public ResponseEntity<DataResponse<Void>> delete(
            @PathVariable UUID trackingId,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(habilitationService.delete(trackingId, user.getUsername()));
    }

    @PostMapping(value = "/import", consumes = "multipart/form-data")
    @Operation(summary = "Importer des habilitations depuis un fichier xlsx")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SE')")
    public ResponseEntity<com.ossanasur.cbconnect.module.auth.dto.response.ImportResultResponse> importXlsx(
            @org.springframework.web.bind.annotation.RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @AuthenticationPrincipal UserDetails user) {
        com.ossanasur.cbconnect.module.auth.dto.response.ImportResultResponse result =
            habilitationImportService.importXlsx(file, user.getUsername());
        int status = result.erreurs().isEmpty() ? 201 : 400;
        return ResponseEntity.status(status).body(result);
    }

    @GetMapping("/template")
    @Operation(summary = "Telecharger un template xlsx vide")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SE')")
    public ResponseEntity<byte[]> downloadTemplate() {
        byte[] bytes = habilitationImportService.generateTemplate();
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=template-habilitations.xlsx")
            .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            .body(bytes);
    }
}
