package com.ossanasur.cbconnect.module.auth.controller;

import com.ossanasur.cbconnect.module.auth.dto.request.UtilisateurRequest;
import com.ossanasur.cbconnect.module.auth.dto.response.UtilisateurResponse;
import com.ossanasur.cbconnect.module.auth.service.UtilisateurService;
import com.ossanasur.cbconnect.security.dto.request.ChangePasswordRequest;
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
@RequestMapping("/v1/utilisateurs")
@RequiredArgsConstructor
@Tag(name = "Utilisateurs", description = "Gestion des agents BNCB")
@SecurityRequirement(name = "bearerAuth")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    @PostMapping
    @Operation(summary = "Creer un utilisateur (envoie un mail d'activation)")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SE')")
    public ResponseEntity<DataResponse<UtilisateurResponse>> create(
            @Valid @RequestBody UtilisateurRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(utilisateurService.creer(request, user.getUsername()));
    }

    @GetMapping("/{trackingId}")
    @Operation(summary = "Obtenir un utilisateur")
    public ResponseEntity<DataResponse<UtilisateurResponse>> getOne(@PathVariable UUID trackingId) {
        return ResponseEntity.ok(utilisateurService.getByTrackingId(trackingId));
    }

    @GetMapping
    @Operation(summary = "Lister tous les utilisateurs actifs")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SE') or hasRole('CSS')")
    public ResponseEntity<DataResponse<List<UtilisateurResponse>>> getAll() {
        return ResponseEntity.ok(utilisateurService.getAll());
    }

    @PutMapping("/{trackingId}")
    @Operation(summary = "Modifier un utilisateur")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SE')")
    public ResponseEntity<DataResponse<UtilisateurResponse>> update(
            @PathVariable UUID trackingId,
            @Valid @RequestBody UtilisateurRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(utilisateurService.modifier(trackingId, request, user.getUsername()));
    }

    @DeleteMapping("/{trackingId}")
    @Operation(summary = "Supprimer logiquement un utilisateur")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SE')")
    public ResponseEntity<DataResponse<Void>> delete(
            @PathVariable UUID trackingId,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(utilisateurService.supprimer(trackingId, user.getUsername()));
    }

    @PutMapping("/{trackingId}/changer-password")
    @Operation(summary = "Changer le mot de passe d'un utilisateur")
    public ResponseEntity<DataResponse<Void>> changerPassword(
            @PathVariable UUID trackingId,
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(utilisateurService.changerPassword(trackingId, request, user.getUsername()));
    }
}
