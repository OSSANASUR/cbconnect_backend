package com.ossanasur.cbconnect.module.delai.controller;

import com.ossanasur.cbconnect.module.delai.dto.request.ParametreDelaiUpdateRequest;
import com.ossanasur.cbconnect.module.delai.dto.request.ParametreSystemeUpdateRequest;
import com.ossanasur.cbconnect.module.delai.dto.response.*;
import com.ossanasur.cbconnect.module.delai.service.DelaiService;
import com.ossanasur.cbconnect.utils.DataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController @RequestMapping("/v1/delais") @RequiredArgsConstructor
@Tag(name = "Délais & Alertes", description = "Suivi des délais CEDEAO + délais internes BNCB")
@SecurityRequirement(name = "bearerAuth")
public class DelaiController {

    private final DelaiService delaiService;

    // ─── Alertes / Notifications ─────────────────────────────────────────────

    @GetMapping("/sinistre/{sinistreId}")
    @Operation(summary = "Lister tous les suivis de délais actifs d'un sinistre")
    public ResponseEntity<DataResponse<List<NotificationDelaiResponse>>> getBySinistre(@PathVariable UUID sinistreId) {
        return ResponseEntity.ok(delaiService.getActiveBySinistre(sinistreId));
    }

    @GetMapping("/urgents") @PreAuthorize("hasAnyRole('SE','CSS')")
    @Operation(summary = "Dossiers en alerte URGENT ou CRITIQUE — tableau de bord prioritaire")
    public ResponseEntity<DataResponse<List<NotificationDelaiResponse>>> getUrgents() {
        return ResponseEntity.ok(delaiService.getUrgents());
    }

    @GetMapping("/mes-alertes")
    @Operation(summary = "Mes propres alertes de délais")
    public ResponseEntity<DataResponse<List<NotificationDelaiResponse>>> getMesAlertes(@AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(delaiService.getMesAlertes(u.getUsername()));
    }

    @PostMapping("/sinistre/{sinistreId}/initialiser") @PreAuthorize("hasAnyRole('SE','CSS')")
    @Operation(summary = "Initialiser manuellement les suivis de délais d'un sinistre")
    public ResponseEntity<DataResponse<Void>> initialiser(@PathVariable UUID sinistreId, @AuthenticationPrincipal UserDetails u) {
        delaiService.initialiserDelaisPourSinistre(sinistreId, u.getUsername());
        return ResponseEntity.ok(DataResponse.success("Délais initialisés", null));
    }

    @PatchMapping("/{id}/resoudre") @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR')")
    @Operation(summary = "Marquer un suivi de délai comme résolu")
    public ResponseEntity<DataResponse<Void>> resoudre(@PathVariable Integer id,
            @RequestParam String motif, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(delaiService.resoudre(id, motif, u.getUsername()));
    }

    @PostMapping("/{id}/relancer") @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR')")
    @Operation(summary = "Envoyer une relance manuelle par mail pour ce délai")
    public ResponseEntity<DataResponse<Void>> relancer(@PathVariable Integer id) {
        return ResponseEntity.ok(delaiService.relancerManuellement(id));
    }

    // ─── Référentiel des paramètres délai ────────────────────────────────────

    @GetMapping("/parametres")
    @Operation(summary = "Lister tous les paramètres de délais (référentiel)")
    public ResponseEntity<DataResponse<List<ParametreDelaiResponse>>> listParametres() {
        return ResponseEntity.ok(delaiService.listParametres());
    }

    @PutMapping("/parametres/{id}") @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Modifier un paramètre de délai (Admin uniquement)")
    public ResponseEntity<DataResponse<ParametreDelaiResponse>> updateParametre(
            @PathVariable Integer id, @RequestBody ParametreDelaiUpdateRequest r) {
        return ResponseEntity.ok(delaiService.updateParametre(id, r));
    }

    // ─── Paramètres système ───────────────────────────────────────────────────

    @GetMapping("/parametres-systeme")
    @Operation(summary = "Lister les paramètres système (frais de gestion, etc.)")
    public ResponseEntity<DataResponse<List<ParametreSystemeResponse>>> listParametresSysteme() {
        return ResponseEntity.ok(delaiService.listParametresSysteme());
    }

    @PutMapping("/parametres-systeme/{id}") @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Modifier un paramètre système (Admin uniquement)")
    public ResponseEntity<DataResponse<ParametreSystemeResponse>> updateParametreSysteme(
            @PathVariable Integer id, @RequestBody ParametreSystemeUpdateRequest r) {
        return ResponseEntity.ok(delaiService.updateParametreSysteme(id, r));
    }
}
