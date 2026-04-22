package com.ossanasur.cbconnect.module.auth.controller;

import com.ossanasur.cbconnect.module.auth.dto.request.ParametreRequest;
import com.ossanasur.cbconnect.module.auth.dto.response.ParametreResponse;
import com.ossanasur.cbconnect.module.auth.service.ParametreService;
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
@RequestMapping("/v1/parametres")
@RequiredArgsConstructor
@Tag(name = "Parametres", description = "Gestion des parametres systeme et listes de reference")
@SecurityRequirement(name = "bearerAuth")
public class ParametreController {

    private final ParametreService parametreService;

    @PostMapping
    @Operation(summary = "Creer un parametre")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SE')")
    public ResponseEntity<DataResponse<ParametreResponse>> create(
            @Valid @RequestBody ParametreRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(parametreService.create(request, user.getUsername()));
    }

    @GetMapping
    @Operation(summary = "Lister tous les parametres actifs")
    public ResponseEntity<DataResponse<List<ParametreResponse>>> getAll() {
        return ResponseEntity.ok(parametreService.getAll());
    }

    @GetMapping("/liste")
    @Operation(summary = "Lister tous les parametres de type LISTE")
    public ResponseEntity<DataResponse<List<ParametreResponse>>> getAllListe() {
        return ResponseEntity.ok(parametreService.getAllListe());
    }

    @GetMapping("/categorie/{categorie}")
    @Operation(summary = "Lister les parametres d'une categorie (prefix de cle)",
               description = "Ex: PROFESSION, MARQUE")
    public ResponseEntity<DataResponse<List<ParametreResponse>>> getByCategorie(
            @PathVariable String categorie) {
        return ResponseEntity.ok(parametreService.getByCategorie(categorie));
    }

    @GetMapping("/cle/{cle}")
    @Operation(summary = "Obtenir un parametre par sa cle")
    public ResponseEntity<DataResponse<ParametreResponse>> getByCle(@PathVariable String cle) {
        return ResponseEntity.ok(parametreService.getByCle(cle));
    }

    @GetMapping("/{trackingId}")
    @Operation(summary = "Obtenir un parametre par trackingId")
    public ResponseEntity<DataResponse<ParametreResponse>> getOne(@PathVariable UUID trackingId) {
        return ResponseEntity.ok(parametreService.getByTrackingId(trackingId));
    }

    @PutMapping("/{trackingId}")
    @Operation(summary = "Modifier un parametre (cree une nouvelle version)")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SE')")
    public ResponseEntity<DataResponse<ParametreResponse>> update(
            @PathVariable UUID trackingId,
            @Valid @RequestBody ParametreRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(parametreService.update(trackingId, request, user.getUsername()));
    }

    @DeleteMapping("/{trackingId}")
    @Operation(summary = "Supprimer logiquement un parametre")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SE')")
    public ResponseEntity<DataResponse<Void>> delete(
            @PathVariable UUID trackingId,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(parametreService.delete(trackingId, user.getUsername()));
    }

    @GetMapping("/{trackingId}/historique")
    @Operation(summary = "Historique des versions d'un parametre")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginatedResponse<ParametreResponse>> historique(
            @PathVariable UUID trackingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(parametreService.getHistory(trackingId, page, size));
    }
}
