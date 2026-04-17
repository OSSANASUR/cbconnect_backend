package com.ossanasur.cbconnect.module.auth.controller;

import com.ossanasur.cbconnect.common.enums.TypeOrganisme;
import com.ossanasur.cbconnect.module.auth.dto.request.OrganismeRequest;
import com.ossanasur.cbconnect.module.auth.dto.response.OrganismeResponse;
import com.ossanasur.cbconnect.module.auth.service.OrganismeService;
import com.ossanasur.cbconnect.utils.DataResponse;
import com.ossanasur.cbconnect.utils.PaginatedResponse;
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

    @GetMapping("/{trackingId}/historique")
    @Operation(summary = "Historique des versions d'un organisme")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginatedResponse<OrganismeResponse>> historique(
            @PathVariable UUID trackingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(organismeService.getHistory(trackingId, page, size));
    }
}
