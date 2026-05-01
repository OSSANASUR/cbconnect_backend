package com.ossanasur.cbconnect.module.finance.controller;

import com.ossanasur.cbconnect.module.finance.dto.request.AnnulerPrefinancementRequest;
import com.ossanasur.cbconnect.module.finance.dto.request.PrefinancementCreateRequest;
import com.ossanasur.cbconnect.module.finance.dto.request.RembourserPrefinancementRequest;
import com.ossanasur.cbconnect.module.finance.dto.response.CouvertureFinanciereResponse;
import com.ossanasur.cbconnect.module.finance.dto.response.PrefinancementDetailResponse;
import com.ossanasur.cbconnect.module.finance.dto.response.PrefinancementResponse;
import com.ossanasur.cbconnect.module.finance.dto.response.RemboursementSuggestionResponse;
import com.ossanasur.cbconnect.module.finance.service.PrefinancementService;
import com.ossanasur.cbconnect.utils.DataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Préfinancements", description = "Gestion des préfinancements Pool (avance trésorerie sur sinistre)")
@RequestMapping("/v1/prefinancements")
public class PrefinancementController {

    private final PrefinancementService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('SE','REDACTEUR')")
    @Operation(summary = "Créer une demande de préfinancement (statut DEMANDE)")
    public ResponseEntity<DataResponse<PrefinancementDetailResponse>> creer(
            @Valid @RequestBody PrefinancementCreateRequest request,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.creer(request, u.getUsername()));
    }

    @GetMapping("/{trackingId}")
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR','COMPTABLE')")
    @Operation(summary = "Détail d'un préfinancement")
    public ResponseEntity<DataResponse<PrefinancementDetailResponse>> getByTrackingId(
            @PathVariable UUID trackingId) {
        return ResponseEntity.ok(service.getByTrackingId(trackingId));
    }

    @PostMapping("/{trackingId}/valider")
    @PreAuthorize("hasAnyRole('SE','REDACTEUR','COMPTABLE')")
    @Operation(summary = "Valider une demande (DEMANDE → VALIDE) — génère écriture PREFINANCEMENT")
    public ResponseEntity<DataResponse<PrefinancementDetailResponse>> valider(
            @PathVariable UUID trackingId,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(service.valider(trackingId, u.getUsername()));
    }

    @PostMapping("/{trackingId}/annuler")
    @PreAuthorize("hasAnyRole('SE','REDACTEUR','COMPTABLE')")
    @Operation(summary = "Annuler un préfinancement (CONTRA_ECRITURE si depuis VALIDE)")
    public ResponseEntity<DataResponse<PrefinancementDetailResponse>> annuler(
            @PathVariable UUID trackingId,
            @Valid @RequestBody AnnulerPrefinancementRequest request,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(service.annuler(trackingId, request, u.getUsername()));
    }

    @GetMapping("/{trackingId}/remboursement-suggere")
    @PreAuthorize("hasAnyRole('SE','REDACTEUR','COMPTABLE')")
    @Operation(summary = "Suggestion de remboursement (montant + encaissements candidats)")
    public ResponseEntity<DataResponse<RemboursementSuggestionResponse>> getRemboursementSuggere(
            @PathVariable UUID trackingId) {
        return ResponseEntity.ok(service.getRemboursementSuggere(trackingId));
    }

    @PostMapping("/{trackingId}/rembourser")
    @PreAuthorize("hasAnyRole('SE','REDACTEUR','COMPTABLE')")
    @Operation(summary = "Imputer un remboursement (encaissement source + montant)")
    public ResponseEntity<DataResponse<PrefinancementDetailResponse>> rembourser(
            @PathVariable UUID trackingId,
            @Valid @RequestBody RembourserPrefinancementRequest request,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(service.rembourser(trackingId, request, u.getUsername()));
    }

    @GetMapping("/sinistre/{sinistreTrackingId}")
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR','COMPTABLE')")
    @Operation(summary = "Liste des préfinancements actifs d'un sinistre")
    public ResponseEntity<DataResponse<List<PrefinancementResponse>>> getBySinistre(
            @PathVariable UUID sinistreTrackingId) {
        return ResponseEntity.ok(service.getBySinistre(sinistreTrackingId));
    }

    @GetMapping("/couverture/{sinistreTrackingId}")
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR','COMPTABLE')")
    @Operation(summary = "Couverture financière d'un sinistre (encaissements + préfinancements + règles A/B/C)")
    public ResponseEntity<DataResponse<CouvertureFinanciereResponse>> getCouvertureFinanciere(
            @PathVariable UUID sinistreTrackingId) {
        return ResponseEntity.ok(service.getCouvertureFinanciere(sinistreTrackingId));
    }
}
