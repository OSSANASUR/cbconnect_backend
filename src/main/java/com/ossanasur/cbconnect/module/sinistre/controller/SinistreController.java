package com.ossanasur.cbconnect.module.sinistre.controller;

import com.ossanasur.cbconnect.module.finance.dto.response.CouvertureSinistreResponse;
import com.ossanasur.cbconnect.module.finance.service.PrefinancementService;
import com.ossanasur.cbconnect.module.sinistre.dto.request.ConfirmationGarantieRequest;
import com.ossanasur.cbconnect.module.sinistre.dto.request.MiseEnArbitrageRequest;
import com.ossanasur.cbconnect.module.sinistre.dto.request.MiseEnContentieuxRequest;
import com.ossanasur.cbconnect.module.sinistre.dto.request.SinistreRequest;
import com.ossanasur.cbconnect.module.sinistre.dto.response.EncaissementStatusResponse;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/v1/sinistres")
@RequiredArgsConstructor
@Tag(name = "Sinistres", description = "Gestion des dossiers sinistres Carte Brune CEDEAO")
@SecurityRequirement(name = "bearerAuth")
public class SinistreController {
    private final SinistreService sinistreService;
    private final PrefinancementService prefinancementService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR','SECRETAIRE')")
    @Operation(summary = "Declarer un nouveau sinistre")
    public ResponseEntity<DataResponse<SinistreResponse>> create(@Valid @RequestBody SinistreRequest r,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(sinistreService.create(r, u.getUsername()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un sinistre")
    public ResponseEntity<DataResponse<SinistreResponse>> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(sinistreService.getByTrackingId(id));
    }

    @GetMapping("/{trackingId}/encaissement-status")
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR','COMPTABLE')")
    @Operation(summary = "Statut encaissement vs règlements pour un sinistre — pilote l'UX du frontend")
    public ResponseEntity<DataResponse<EncaissementStatusResponse>> getEncaissementStatus(
            @PathVariable UUID trackingId) {
        return ResponseEntity.ok(sinistreService.getEncaissementStatus(trackingId));
    }

    @GetMapping
    @Operation(summary = "Lister les sinistres (search optionnel : numéro, assuré, immatriculation, police)")
    public ResponseEntity<PaginatedResponse<SinistreResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String positionRc,
            @RequestParam(required = false) String rcPct,
            @RequestParam(required = false) String litige,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        return ResponseEntity.ok(sinistreService.getAllFiltered(
                search, statut, positionRc, rcPct, litige, dateDebut, dateFin, page, size));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR')")
    @Operation(summary = "Modifier un sinistre")
    public ResponseEntity<DataResponse<SinistreResponse>> update(@PathVariable UUID id,
            @Valid @RequestBody SinistreRequest r, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(sinistreService.update(id, r, u.getUsername()));
    }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR')")
    @Operation(summary = "Changer le statut d'un sinistre")
    public ResponseEntity<DataResponse<Void>> changerStatut(@PathVariable UUID id, @RequestParam String statut,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(sinistreService.changerStatut(id, statut, u.getUsername()));
    }

    /* V27 : la position RC se gère adversaire par adversaire.
       Endpoint déplacé : PATCH /v1/victimes/{adversaireId}/position-rc */

    @PatchMapping("/{id}/confirmer-garantie")
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR','GESTIONNAIRE')")
    @Operation(summary = "Confirmer la garantie (acquise ou non) et changer le statut en GARANTIE_CONFIRMEE ou GARANTIE_NON_ACQUISE")
    public ResponseEntity<DataResponse<SinistreResponse>> confirmerGarantie(
            @PathVariable UUID id,
            @Valid @RequestBody ConfirmationGarantieRequest r,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(sinistreService.confirmerGarantie(id, r, u.getUsername()));
    }

    @PatchMapping("/{id}/assigner")
    @PreAuthorize("hasAnyRole('SE','CSS')")
    @Operation(summary = "Assigner un redacteur au sinistre")
    public ResponseEntity<DataResponse<Void>> assigner(@PathVariable UUID id, @RequestParam UUID redacteurId,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(sinistreService.assignerRedacteur(id, redacteurId, u.getUsername()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SE')")
    @Operation(summary = "Supprimer logiquement un sinistre")
    public ResponseEntity<DataResponse<Void>> delete(@PathVariable UUID id, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(sinistreService.delete(id, u.getUsername()));
    }

    /* ═════════ Passages en CONTENTIEUX / ARBITRAGE / sortie vers BAP ═════════ */

    @PatchMapping("/{id}/contentieux")
    @PreAuthorize("hasAnyRole('SE','CSS')")
    @Operation(summary = "Mettre le dossier en CONTENTIEUX (procédure judiciaire). Capture niveau de juridiction et prochaine audience.")
    public ResponseEntity<DataResponse<SinistreResponse>> mettreEnContentieux(
            @PathVariable UUID id,
            @Valid @RequestBody MiseEnContentieuxRequest r,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(sinistreService.mettreEnContentieux(id, r, u.getUsername()));
    }

    @PatchMapping("/{id}/arbitrage")
    @PreAuthorize("hasAnyRole('SE','CSS')")
    @Operation(summary = "Mettre le dossier en ARBITRAGE (instance arbitrale / commission).")
    public ResponseEntity<DataResponse<SinistreResponse>> mettreEnArbitrage(
            @PathVariable UUID id,
            @Valid @RequestBody MiseEnArbitrageRequest r,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(sinistreService.mettreEnArbitrage(id, r, u.getUsername()));
    }

    @PatchMapping("/{id}/sortir-litige")
    @PreAuthorize("hasAnyRole('SE','CSS')")
    @Operation(summary = "Sortir un dossier du contentieux/arbitrage. Statut → BAP, réintègre la file normale.")
    public ResponseEntity<DataResponse<SinistreResponse>> sortirDuLitige(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(sinistreService.sortirDuLitige(id, u.getUsername()));
    }

    @GetMapping("/{trackingId}/couverture")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Couverture financière complète du sinistre (encaissements, préfis, soldes par règles)")
    public ResponseEntity<DataResponse<CouvertureSinistreResponse>> getCouverture(
            @PathVariable UUID trackingId) {
        return ResponseEntity.ok(prefinancementService.getCouvertureSinistre(trackingId));
    }
}
