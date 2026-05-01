package com.ossanasur.cbconnect.module.sinistre.controller;

import com.ossanasur.cbconnect.common.enums.TypeEntiteConstat;
import com.ossanasur.cbconnect.module.sinistre.dto.request.EntiteConstatRequest;
import com.ossanasur.cbconnect.module.sinistre.dto.response.EntiteConstatImportResponse;
import com.ossanasur.cbconnect.module.sinistre.dto.response.EntiteConstatResponse;
import com.ossanasur.cbconnect.module.sinistre.service.EntiteConstatService;
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
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/entites-constat")
@RequiredArgsConstructor
@Tag(name = "Entites de constat", description = "Referentiel des unites de police/gendarmerie redactrices de PV")
@SecurityRequirement(name = "bearerAuth")
public class EntiteConstatController {
    private final EntiteConstatService entiteConstatService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SE','ADMIN')")
    @Operation(summary = "Creer une entite de constat")
    public ResponseEntity<DataResponse<EntiteConstatResponse>> create(@Valid @RequestBody EntiteConstatRequest r,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(entiteConstatService.create(r, u.getUsername()));
    }

    @PostMapping(value = "/import", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('SE','ADMIN')")
    @Operation(summary = "Importer les entites de constat depuis un fichier Excel")
    public ResponseEntity<DataResponse<EntiteConstatImportResponse>> importXlsx(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails u) {
        DataResponse<EntiteConstatImportResponse> response = entiteConstatService.importXlsx(file, u.getUsername());
        return ResponseEntity.status(response.isSuccess() ? 201 : 400).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recuperer une entite de constat par son trackingId")
    public ResponseEntity<DataResponse<EntiteConstatResponse>> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(entiteConstatService.getByTrackingId(id));
    }

    @GetMapping
    @Operation(summary = "Lister les entites de constat (actifs uniquement par defaut)")
    public ResponseEntity<DataResponse<List<EntiteConstatResponse>>> getAll(
            @RequestParam(defaultValue = "true") boolean actifsOnly) {
        return ResponseEntity.ok(entiteConstatService.getAll(actifsOnly));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Lister les entites de constat par type (POLICE, GENDARMERIE, MIXTE)")
    public ResponseEntity<DataResponse<List<EntiteConstatResponse>>> getByType(@PathVariable TypeEntiteConstat type) {
        return ResponseEntity.ok(entiteConstatService.getByType(type));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SE','ADMIN')")
    @Operation(summary = "Mettre a jour une entite de constat")
    public ResponseEntity<DataResponse<EntiteConstatResponse>> update(@PathVariable UUID id,
            @Valid @RequestBody EntiteConstatRequest r, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(entiteConstatService.update(id, r, u.getUsername()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SE','ADMIN')")
    @Operation(summary = "Supprimer logiquement une entite de constat")
    public ResponseEntity<DataResponse<Void>> delete(@PathVariable UUID id, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(entiteConstatService.delete(id, u.getUsername()));
    }
}
