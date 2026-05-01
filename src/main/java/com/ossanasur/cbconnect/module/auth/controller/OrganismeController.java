package com.ossanasur.cbconnect.module.auth.controller;

import com.ossanasur.cbconnect.common.enums.TypeOrganisme;
import com.ossanasur.cbconnect.module.auth.dto.request.BrandingImageType;
import com.ossanasur.cbconnect.module.auth.dto.request.OrganismeRequest;
import com.ossanasur.cbconnect.module.auth.dto.response.OrganismeResponse;
import com.ossanasur.cbconnect.module.auth.service.OrganismeService;
import com.ossanasur.cbconnect.security.dto.request.TwoFactorUpdateRequest;
import com.ossanasur.cbconnect.security.dto.response.TwoFactorStatusResponse;
import com.ossanasur.cbconnect.utils.DataResponse;
import com.ossanasur.cbconnect.utils.PaginatedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
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
@RequestMapping("/v1/organismes")
@RequiredArgsConstructor
@Tag(name = "Organismes", description = "Gestion des bureaux nationaux, homologues et compagnies membres")
@SecurityRequirement(name = "bearerAuth")
public class OrganismeController {

    private final OrganismeService organismeService;

    @PostMapping
    @Operation(summary = "Creer un organisme")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SE')")
    public ResponseEntity<DataResponse<OrganismeResponse>> create(
            @Valid @RequestBody OrganismeRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(organismeService.create(request, user.getUsername()));
    }

    @GetMapping("/{trackingId}")
    @Operation(summary = "Obtenir un organisme par trackingId")
    public ResponseEntity<DataResponse<OrganismeResponse>> getOne(@PathVariable UUID trackingId) {
        return ResponseEntity.ok(organismeService.getByTrackingId(trackingId));
    }

    @GetMapping
    @Operation(summary = "Lister tous les organismes actifs")
    public ResponseEntity<DataResponse<List<OrganismeResponse>>> getAll() {
        return ResponseEntity.ok(organismeService.getAll());
    }

    @GetMapping("/by-type")
    public ResponseEntity<DataResponse<List<OrganismeResponse>>> list(
            @RequestParam(required = false, name = "typeOrganisme") List<TypeOrganisme> types) {
        return ResponseEntity.ok(organismeService.listerActifs(types));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Lister les organismes par type")
    public ResponseEntity<DataResponse<List<OrganismeResponse>>> getByType(@PathVariable TypeOrganisme type) {
        return ResponseEntity.ok(organismeService.getAllByType(type));
    }

    @PutMapping("/{trackingId}")
    @Operation(summary = "Modifier un organisme (cree une nouvelle version)")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SE')")
    public ResponseEntity<DataResponse<OrganismeResponse>> update(
            @PathVariable UUID trackingId,
            @Valid @RequestBody OrganismeRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(organismeService.update(trackingId, request, user.getUsername()));
    }

    @DeleteMapping("/{trackingId}")
    @Operation(summary = "Supprimer logiquement un organisme")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SE')")
    public ResponseEntity<DataResponse<Void>> delete(
            @PathVariable UUID trackingId,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(organismeService.delete(trackingId, user.getUsername()));
    }

    @GetMapping("/{trackingId}/two-factor")
    @Operation(summary = "Statut de la double authentification d'un organisme")
    public ResponseEntity<DataResponse<TwoFactorStatusResponse>> getTwoFactor(@PathVariable UUID trackingId) {
        return ResponseEntity.ok(organismeService.getTwoFactor(trackingId));
    }

    @PutMapping("/{trackingId}/two-factor")
    @Operation(summary = "Activer/desactiver la double authentification (habilitation ORGANISMES_2FA_MANAGE)")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SE')")
    public ResponseEntity<DataResponse<TwoFactorStatusResponse>> updateTwoFactor(
            @PathVariable UUID trackingId,
            @Valid @RequestBody TwoFactorUpdateRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(organismeService.updateTwoFactor(trackingId, request.enabled(), user.getUsername()));
    }

    @GetMapping("/{trackingId}/historique")
    @Operation(summary = "Historique des versions d'un organisme")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginatedResponse<OrganismeResponse>> historique(
            @PathVariable UUID trackingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(organismeService.getHistory(trackingId, page, size));
    }

    @PostMapping(value = "/{trackingId}/branding/{type}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Téléverser une image de branding (logo / header / footer) pour l'organisme")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SE')")
    public ResponseEntity<DataResponse<OrganismeResponse>> uploadBranding(
            @PathVariable UUID trackingId,
            @PathVariable BrandingImageType type,
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(organismeService.uploadBrandingImage(trackingId, type, file, user.getUsername()));
    }

    @DeleteMapping("/{trackingId}/branding/{type}")
    @Operation(summary = "Supprimer une image de branding pour l'organisme")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SE')")
    public ResponseEntity<DataResponse<OrganismeResponse>> deleteBranding(
            @PathVariable UUID trackingId,
            @PathVariable BrandingImageType type,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(organismeService.deleteBrandingImage(trackingId, type, user.getUsername()));
    }

    @GetMapping("/{trackingId}/branding/{type}")
    @Operation(summary = "Servir une image de branding (accès public — utilisé dans les documents imprimés)")
    public ResponseEntity<Resource> downloadBranding(
            @PathVariable UUID trackingId,
            @PathVariable BrandingImageType type) {
        return organismeService.downloadBrandingImage(trackingId, type);
    }
}
