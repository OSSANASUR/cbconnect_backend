package com.ossanasur.cbconnect.module.reclamation.controller;

import com.ossanasur.cbconnect.common.enums.StatutDossierReclamation;
import com.ossanasur.cbconnect.module.reclamation.dto.request.*;
import com.ossanasur.cbconnect.module.reclamation.dto.response.*;
import com.ossanasur.cbconnect.module.reclamation.service.ReclamationService;
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
@RequestMapping("/v1/dossiers-reclamation")
@RequiredArgsConstructor
@Tag(name = "Dossiers de réclamation",
     description = "Gestion des dossiers de réclamation par victime (pièces admin + factures)")
@SecurityRequirement(name = "bearerAuth")
public class ReclamationController {

    private final ReclamationService reclamationService;

    // ── Dossiers ─────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR')")
    @Operation(summary = "Ouvrir un dossier de réclamation pour une victime")
    public ResponseEntity<DataResponse<DossierReclamationResponse>> ouvrirDossier(
            @Valid @RequestBody DossierReclamationRequest r,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(reclamationService.ouvrirDossier(r, u.getUsername()));
    }

    @GetMapping
    @Operation(summary = "Liste paginée des dossiers de réclamation (filtres : statut, recherche)")
    public ResponseEntity<PaginatedResponse<DossierReclamationResponse>> lister(
            @RequestParam(required = false) StatutDossierReclamation statut,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(reclamationService.listerDossiers(statut, search, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DataResponse<DossierReclamationResponse>> getDossier(@PathVariable UUID id) {
        return ResponseEntity.ok(reclamationService.getDossier(id));
    }

    @GetMapping("/victime/{victimeId}")
    public ResponseEntity<DataResponse<List<DossierReclamationResponse>>> getByVictime(
            @PathVariable UUID victimeId) {
        return ResponseEntity.ok(reclamationService.getDossiersByVictime(victimeId));
    }

    @PatchMapping("/{id}/cloturer")
    @PreAuthorize("hasAnyRole('SE','CSS')")
    @Operation(summary = "Clôturer un dossier de réclamation")
    public ResponseEntity<DataResponse<Void>> cloturer(
            @PathVariable UUID id, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(reclamationService.clotureDossier(id, u.getUsername()));
    }

    // ── Factures (ajoutées sous un dossier) ─────────────────────────

    @PostMapping("/{dossierId}/factures")
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR')")
    @Operation(summary = "Ajouter une facture à un dossier de réclamation")
    public ResponseEntity<DataResponse<FactureReclamationResponse>> ajouterFacture(
            @PathVariable UUID dossierId,
            @Valid @RequestBody FactureReclamationRequest r,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(reclamationService.ajouterFactureDansDossier(dossierId, r, u.getUsername()));
    }

    @GetMapping("/{dossierId}/factures")
    @Operation(summary = "Liste des factures d'un dossier")
    public ResponseEntity<DataResponse<List<FactureReclamationResponse>>> getFactures(
            @PathVariable UUID dossierId) {
        return ResponseEntity.ok(reclamationService.getFacturesByDossier(dossierId));
    }
}
