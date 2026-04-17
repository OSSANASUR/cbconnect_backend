package com.ossanasur.cbconnect.module.sinistre.controller;
import com.ossanasur.cbconnect.module.sinistre.dto.request.SinistreRequest;
import com.ossanasur.cbconnect.module.sinistre.dto.response.SinistreResponse;
import com.ossanasur.cbconnect.module.sinistre.service.SinistreService;
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
import java.util.UUID;
@RestController @RequestMapping("/v1/sinistres") @RequiredArgsConstructor
@Tag(name="Sinistres",description="Gestion des dossiers sinistres Carte Brune CEDEAO")
@SecurityRequirement(name="bearerAuth")
public class SinistreController {
    private final SinistreService sinistreService;
    @PostMapping @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR','SECRETAIRE')")
    @Operation(summary="Declarer un nouveau sinistre")
    public ResponseEntity<DataResponse<SinistreResponse>> create(@Valid @RequestBody SinistreRequest r, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(sinistreService.create(r, u.getUsername())); }
    @GetMapping("/{id}") @Operation(summary="Obtenir un sinistre")
    public ResponseEntity<DataResponse<SinistreResponse>> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(sinistreService.getByTrackingId(id)); }
    @GetMapping @Operation(summary="Lister tous les sinistres")
    public ResponseEntity<PaginatedResponse<SinistreResponse>> getAll(
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        return ResponseEntity.ok(sinistreService.getAll(page, size)); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR')")
    @Operation(summary="Modifier un sinistre")
    public ResponseEntity<DataResponse<SinistreResponse>> update(@PathVariable UUID id, @Valid @RequestBody SinistreRequest r, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(sinistreService.update(id, r, u.getUsername())); }
    @PatchMapping("/{id}/statut") @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR')")
    @Operation(summary="Changer le statut d'un sinistre")
    public ResponseEntity<DataResponse<Void>> changerStatut(@PathVariable UUID id, @RequestParam String statut, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(sinistreService.changerStatut(id, statut, u.getUsername())); }
    @PatchMapping("/{id}/assigner") @PreAuthorize("hasAnyRole('SE','CSS')")
    @Operation(summary="Assigner un redacteur au sinistre")
    public ResponseEntity<DataResponse<Void>> assigner(@PathVariable UUID id, @RequestParam UUID redacteurId, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(sinistreService.assignerRedacteur(id, redacteurId, u.getUsername())); }
    @DeleteMapping("/{id}") @PreAuthorize("hasRole('SE')")
    @Operation(summary="Supprimer logiquement un sinistre")
    public ResponseEntity<DataResponse<Void>> delete(@PathVariable UUID id, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(sinistreService.delete(id, u.getUsername())); }
}
