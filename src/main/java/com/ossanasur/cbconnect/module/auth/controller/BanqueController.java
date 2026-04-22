package com.ossanasur.cbconnect.module.auth.controller;

import com.ossanasur.cbconnect.module.auth.dto.request.BanqueRequest;
import com.ossanasur.cbconnect.module.auth.dto.response.BanqueResponse;
import com.ossanasur.cbconnect.module.auth.service.BanqueService;
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
@RequestMapping("/v1/banques")
@RequiredArgsConstructor
@Tag(name = "Banques", description = "Référentiel des banques partenaires")
@SecurityRequirement(name = "bearerAuth")
public class BanqueController {

    private final BanqueService banqueService;

    @PostMapping
    @Operation(summary = "Créer une banque")
    @PreAuthorize("hasAnyRole('ADMIN','SE')")
    public ResponseEntity<DataResponse<BanqueResponse>> create(
            @Valid @RequestBody BanqueRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(banqueService.create(request, user.getUsername()));
    }

    @GetMapping
    @Operation(summary = "Lister toutes les banques actives")
    public ResponseEntity<DataResponse<List<BanqueResponse>>> getAll() {
        return ResponseEntity.ok(banqueService.getAll());
    }

    @GetMapping("/{trackingId}")
    @Operation(summary = "Obtenir une banque par trackingId")
    public ResponseEntity<DataResponse<BanqueResponse>> getOne(@PathVariable UUID trackingId) {
        return ResponseEntity.ok(banqueService.getByTrackingId(trackingId));
    }

    @PutMapping("/{trackingId}")
    @Operation(summary = "Modifier une banque (crée une nouvelle version)")
    @PreAuthorize("hasAnyRole('ADMIN','SE')")
    public ResponseEntity<DataResponse<BanqueResponse>> update(
            @PathVariable UUID trackingId,
            @Valid @RequestBody BanqueRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(banqueService.update(trackingId, request, user.getUsername()));
    }

    @DeleteMapping("/{trackingId}")
    @Operation(summary = "Supprimer logiquement une banque")
    @PreAuthorize("hasAnyRole('ADMIN','SE')")
    public ResponseEntity<DataResponse<Void>> delete(
            @PathVariable UUID trackingId,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(banqueService.delete(trackingId, user.getUsername()));
    }
}
