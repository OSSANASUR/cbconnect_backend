package com.ossanasur.cbconnect.module.finance.controller;

import com.ossanasur.cbconnect.common.enums.StatutLotReglement;
import com.ossanasur.cbconnect.module.finance.dto.request.CreerLotRequest;
import com.ossanasur.cbconnect.module.finance.dto.request.SaisieComptableLotRequest;
import com.ossanasur.cbconnect.module.finance.dto.response.LotReglementResponse;
import com.ossanasur.cbconnect.module.finance.dto.response.SinistrePayableResponse;
import com.ossanasur.cbconnect.module.finance.service.LotReglementService;
import com.ossanasur.cbconnect.utils.DataResponse;
import com.ossanasur.cbconnect.utils.PaginatedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@RequestMapping("/v1/lot-reglements")
@RequiredArgsConstructor
@Tag(name = "Lots de règlement", description = "Workflow en 2 phases : validation technique (rédacteur) puis saisie et validation comptable.")
@SecurityRequirement(name = "bearerAuth")
public class LotReglementController {

    private final LotReglementService service;

    /**
     * Liste les sinistres payables pour un expert donné.
     * Rôles : SE, CSS, REDACTEUR, COMPTABLE
     */
    @GetMapping("/sinistres-payables")
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR','COMPTABLE')")
    @Operation(summary = "Lister les sinistres payables pour un expert")
    public ResponseEntity<DataResponse<List<SinistrePayableResponse>>> listerSinistresPayables(
            @RequestParam UUID expertTrackingId) {
        return ResponseEntity.ok(service.listerSinistresPayables(expertTrackingId));
    }

    /**
     * Crée un lot de règlement (phase technique).
     * Statut résultant : EN_COURS
     * Rôles : SE, REDACTEUR
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SE','REDACTEUR')")
    @Operation(summary = "Créer un lot de règlement (phase technique 1/2)")
    public ResponseEntity<DataResponse<LotReglementResponse>> creerLot(
            @RequestBody @Valid CreerLotRequest req,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.creerLot(req, principal.getUsername()));
    }

    /**
     * Valide techniquement un lot de règlement.
     * Statut résultant : VALIDE_TECHNIQUE
     * Rôles : SE, REDACTEUR
     */
    @PostMapping("/{lotTrackingId}/valider-technique")
    @PreAuthorize("hasAnyRole('SE','REDACTEUR')")
    @Operation(summary = "Valider techniquement un lot de règlement")
    public ResponseEntity<DataResponse<LotReglementResponse>> validerTechnique(
            @PathVariable UUID lotTrackingId,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(service.validerTechniqueLot(lotTrackingId, principal.getUsername()));
    }

    /**
     * Saisit les informations comptables d'un lot de règlement (phase comptable).
     * Rôles : SE, COMPTABLE
     */
    @PostMapping("/{lotTrackingId}/saisir-comptable")
    @PreAuthorize("hasAnyRole('SE','COMPTABLE')")
    @Operation(summary = "Saisir les informations comptables d'un lot de règlement (phase comptable 2/2)")
    public ResponseEntity<DataResponse<LotReglementResponse>> saisirComptable(
            @PathVariable UUID lotTrackingId,
            @RequestBody @Valid SaisieComptableLotRequest req,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(service.saisirComptableLot(lotTrackingId, req, principal.getUsername()));
    }

    /**
     * Valide comptablement un lot de règlement.
     * Statut résultant : VALIDE_COMPTABLE
     * Rôles : SE, COMPTABLE
     */
    @PostMapping("/{lotTrackingId}/valider-comptable")
    @PreAuthorize("hasAnyRole('SE','COMPTABLE')")
    @Operation(summary = "Valider comptablement un lot de règlement")
    public ResponseEntity<DataResponse<LotReglementResponse>> validerComptable(
            @PathVariable UUID lotTrackingId,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(service.validerComptableLot(lotTrackingId, principal.getUsername()));
    }

    /**
     * Récupère le détail d'un lot de règlement par son tracking ID.
     * Rôles : SE, CSS, REDACTEUR, COMPTABLE
     */
    @GetMapping("/{lotTrackingId}")
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR','COMPTABLE')")
    @Operation(summary = "Détail d'un lot de règlement")
    public ResponseEntity<DataResponse<LotReglementResponse>> detail(
            @PathVariable UUID lotTrackingId) {
        return ResponseEntity.ok(service.getByTrackingId(lotTrackingId));
    }

    /**
     * Liste les lots de règlement contenant au moins un paiement sur ce sinistre.
     * Utilisé par OngletReglements (section "Règlements par lot").
     */
    @GetMapping("/by-sinistre/{sinistreTrackingId}")
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR','COMPTABLE')")
    @Operation(summary = "Lister les lots ayant un paiement sur ce sinistre")
    public ResponseEntity<DataResponse<List<LotReglementResponse>>> listerBySinistre(
            @PathVariable UUID sinistreTrackingId) {
        return ResponseEntity.ok(service.listerBySinistre(sinistreTrackingId));
    }

    /**
     * Liste les lots de règlement avec filtres optionnels.
     * Rôles : SE, CSS, REDACTEUR, COMPTABLE
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR','COMPTABLE')")
    @Operation(summary = "Lister les lots de règlement")
    public ResponseEntity<PaginatedResponse<LotReglementResponse>> lister(
            @RequestParam(required = false) UUID expertTrackingId,
            @RequestParam(required = false) StatutLotReglement statut,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.lister(expertTrackingId, statut, page, size));
    }
}
