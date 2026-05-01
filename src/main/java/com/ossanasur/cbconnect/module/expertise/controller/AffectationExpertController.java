package com.ossanasur.cbconnect.module.expertise.controller;

import com.ossanasur.cbconnect.module.expertise.dto.request.AffectationExpertRequest;
import com.ossanasur.cbconnect.module.expertise.dto.response.AffectationExpertResponse;
import com.ossanasur.cbconnect.module.expertise.service.impl.AffectationExpertServiceImpl;
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
@RequestMapping("/v1/affectations-expert")
@RequiredArgsConstructor
@Tag(name = "Affectations Expert", description = "Affectation d'un expert à une victime — génère note de mission + lettre prévenance")
@SecurityRequirement(name = "bearerAuth")
public class AffectationExpertController {

    private final AffectationExpertServiceImpl service;

    @PostMapping
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR')")
    @Operation(summary = "Affecter un expert à une victime — génère 2 courriers automatiquement")
    public ResponseEntity<DataResponse<AffectationExpertResponse>> affecter(
            @Valid @RequestBody AffectationExpertRequest req,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(service.affecter(req, u.getUsername()));
    }

    @GetMapping("/sinistre/{sinistreId}")
    @Operation(summary = "Affectations d'un sinistre")
    public ResponseEntity<DataResponse<List<AffectationExpertResponse>>> getBySinistre(
            @PathVariable UUID sinistreId) {
        return ResponseEntity.ok(service.getBySinistre(sinistreId));
    }

    @GetMapping("/victime/{victimeId}")
    @Operation(summary = "Affectations d'une victime")
    public ResponseEntity<DataResponse<List<AffectationExpertResponse>>> getByVictime(
            @PathVariable UUID victimeId) {
        return ResponseEntity.ok(service.getByVictime(victimeId));
    }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR')")
    @Operation(summary = "Mettre à jour le statut (EN_ATTENTE → RAPPORT_RECU → CLOTURE)")
    public ResponseEntity<DataResponse<AffectationExpertResponse>> updateStatut(
            @PathVariable UUID id,
            @RequestParam String statut,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(service.mettreAJourStatut(id, statut, u.getUsername()));
    }

    @PostMapping("/{id}/envoyer-mail-expert")
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR')")
    @Operation(summary = "Marquer le mail expert comme envoyé")
    public ResponseEntity<DataResponse<AffectationExpertResponse>> envoyerMailExpert(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(service.marquerMailExpertEnvoye(id, u.getUsername()));
    }
}