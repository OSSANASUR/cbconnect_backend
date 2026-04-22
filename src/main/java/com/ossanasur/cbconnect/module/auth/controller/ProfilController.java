package com.ossanasur.cbconnect.module.auth.controller;

import com.ossanasur.cbconnect.module.auth.dto.request.ProfilRequest;
import com.ossanasur.cbconnect.module.auth.dto.response.ProfilResponse;
import com.ossanasur.cbconnect.module.auth.service.ProfilService;
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
@RequestMapping("/v1/profils")
@RequiredArgsConstructor
@Tag(name = "Profils", description = "Gestion des profils RBAC et leurs habilitations")
@SecurityRequirement(name = "bearerAuth")
public class ProfilController {

    private final ProfilService profilService;

    @PostMapping
    @Operation(summary = "Creer un profil")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SE')")
    public ResponseEntity<DataResponse<ProfilResponse>> create(
            @Valid @RequestBody ProfilRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(profilService.create(request, user.getUsername()));
    }

    @GetMapping
    @Operation(summary = "Lister tous les profils actifs")
    public ResponseEntity<DataResponse<List<ProfilResponse>>> getAll() {
        return ResponseEntity.ok(profilService.getAll());
    }

    @GetMapping("/organisme/{organismeTrackingId}")
    @Operation(summary = "Lister les profils d'un organisme")
    public ResponseEntity<DataResponse<List<ProfilResponse>>> getByOrganisme(
            @PathVariable UUID organismeTrackingId) {
        return ResponseEntity.ok(profilService.getByOrganisme(organismeTrackingId));
    }

    @GetMapping("/{trackingId}")
    @Operation(summary = "Obtenir un profil par trackingId")
    public ResponseEntity<DataResponse<ProfilResponse>> getOne(@PathVariable UUID trackingId) {
        return ResponseEntity.ok(profilService.getByTrackingId(trackingId));
    }

    @PutMapping("/{trackingId}")
    @Operation(summary = "Modifier un profil (cree une nouvelle version)")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SE')")
    public ResponseEntity<DataResponse<ProfilResponse>> update(
            @PathVariable UUID trackingId,
            @Valid @RequestBody ProfilRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(profilService.update(trackingId, request, user.getUsername()));
    }

    @DeleteMapping("/{trackingId}")
    @Operation(summary = "Supprimer logiquement un profil")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SE')")
    public ResponseEntity<DataResponse<Void>> delete(
            @PathVariable UUID trackingId,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(profilService.delete(trackingId, user.getUsername()));
    }

    @GetMapping("/{trackingId}/historique")
    @Operation(summary = "Historique des versions d'un profil")
    public ResponseEntity<PaginatedResponse<ProfilResponse>> historique(
            @PathVariable UUID trackingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(profilService.getHistory(trackingId, page, size));
    }
}
