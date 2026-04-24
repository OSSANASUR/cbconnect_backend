package com.ossanasur.cbconnect.module.finance.controller;

import com.ossanasur.cbconnect.common.enums.StatutPaiement;
import com.ossanasur.cbconnect.module.finance.dto.request.AnnulerPaiementRequest;
import com.ossanasur.cbconnect.module.finance.dto.request.PaiementCreateRequest;
import com.ossanasur.cbconnect.module.finance.dto.request.ReglementComptableRequest;
import com.ossanasur.cbconnect.module.finance.dto.response.PaiementDetailResponse;
import com.ossanasur.cbconnect.module.finance.dto.response.PaiementResponse;
import com.ossanasur.cbconnect.module.finance.service.PaiementService;
import com.ossanasur.cbconnect.utils.DataResponse;
import com.ossanasur.cbconnect.utils.PaginatedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/paiements")
@RequiredArgsConstructor
@Tag(name = "Paiements", description = "Workflow en 2 phases : technique (rédacteur) puis comptable.")
@SecurityRequirement(name = "bearerAuth")
public class PaiementController {

    private final PaiementService paiementService;

    /**
     * Crée un règlement technique (bénéficiaire + montant).
     * Statut résultant : EMIS
     * Rôles : SE, REDACTEUR
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SE','REDACTEUR')")
    @Operation(summary = "Créer un règlement technique (phase 1/2)")
    public ResponseEntity<DataResponse<PaiementDetailResponse>> creer(
            @Valid @RequestBody PaiementCreateRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paiementService.creer(request, principal.getUsername()));
    }

    /**
     * Valide le règlement technique.
     * Statut résultant : REGLEMENT_TECHNIQUE_VALIDE
     * Génère l'Ordre de Dépense (document imprimable).
     * Rôles : SE, REDACTEUR
     */
    @PostMapping("/{trackingId}/valider-technique")
    @PreAuthorize("hasAnyRole('SE','REDACTEUR')")
    @Operation(summary = "Valider le règlement technique → génère l'Ordre de Dépense")
    public ResponseEntity<DataResponse<PaiementDetailResponse>> validerTechnique(
            @PathVariable UUID trackingId,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(paiementService.validerTechnique(trackingId, principal.getUsername()));
    }

    /**
     * Saisit les informations comptables (chèque, dates).
     * Statut résultant : REGLEMENT_COMPTABLE_VALIDE
     * Rôles : COMPTABLE
     */
    @PostMapping("/{trackingId}/reglement-comptable")
    @PreAuthorize("hasAnyRole('SE','COMPTABLE')")
    @Operation(summary = "Saisir le règlement comptable (chèque + dates) → phase 2/2")
    public ResponseEntity<DataResponse<PaiementDetailResponse>> saisirReglementComptable(
            @PathVariable UUID trackingId,
            @Valid @RequestBody ReglementComptableRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(
                paiementService.saisirReglementComptable(trackingId, request, principal.getUsername()));
    }

    /**
     * Valide le règlement comptable.
     * Statut résultant : PAYE
     * Génère l'écriture comptable + la Quittance d'indemnité.
     * Rôles : COMPTABLE
     */
    @PostMapping("/{trackingId}/valider-comptable")
    @PreAuthorize("hasAnyRole('SE','COMPTABLE')")
    @Operation(summary = "Valider le règlement comptable → génère écriture + Quittance")
    public ResponseEntity<DataResponse<PaiementDetailResponse>> validerComptable(
            @PathVariable UUID trackingId,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(paiementService.validerComptable(trackingId, principal.getUsername()));
    }

    @PostMapping("/{trackingId}/annuler")
    @PreAuthorize("hasAnyRole('SE','REDACTEUR','COMPTABLE')")
    @Operation(summary = "Annuler un règlement (contre-écriture si écriture déjà validée)")
    public ResponseEntity<DataResponse<PaiementDetailResponse>> annuler(
            @PathVariable UUID trackingId,
            @Valid @RequestBody AnnulerPaiementRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(paiementService.annuler(trackingId, request, principal.getUsername()));
    }

    @GetMapping("/{trackingId}")
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR','COMPTABLE')")
    @Operation(summary = "Détail d'un règlement")
    public ResponseEntity<DataResponse<PaiementDetailResponse>> getOne(@PathVariable UUID trackingId) {
        return ResponseEntity.ok(paiementService.getByTrackingId(trackingId));
    }

    @GetMapping("/sinistre/{sinistreTrackingId}")
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR','COMPTABLE')")
    @Operation(summary = "Règlements rattachés à un dossier sinistre")
    public ResponseEntity<DataResponse<List<PaiementResponse>>> getBySinistre(
            @PathVariable UUID sinistreTrackingId) {
        return ResponseEntity.ok(paiementService.getBySinistre(sinistreTrackingId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR','COMPTABLE')")
    @Operation(summary = "Recherche paginée avec filtres optionnels")
    public ResponseEntity<PaginatedResponse<PaiementResponse>> rechercher(
            @RequestParam(required = false) StatutPaiement statut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(required = false) UUID sinistreTrackingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                paiementService.rechercher(statut, dateDebut, dateFin, sinistreTrackingId, page, size));
    }

    @PostMapping("/{trackingId}/encaissements/{encaissementTrackingId}")
    @PreAuthorize("hasAnyRole('SE','COMPTABLE')")
    @Operation(summary = "Rapprocher un encaissement à un règlement payé")

    public ResponseEntity<DataResponse<PaiementDetailResponse>> lierEncaissement(
            @PathVariable UUID trackingId,
            @PathVariable UUID encaissementTrackingId,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(
                paiementService.lierEncaissement(trackingId, encaissementTrackingId, principal.getUsername()));
    }

    @DeleteMapping("/{trackingId}/encaissements/{encaissementTrackingId}")
    @PreAuthorize("hasRole('COMPTABLE')")
    @Operation(summary = "Détacher un encaissement précédemment rapproché")
    public ResponseEntity<DataResponse<PaiementDetailResponse>> delierEncaissement(
            @PathVariable UUID trackingId,
            @PathVariable UUID encaissementTrackingId,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(
                paiementService.delierEncaissement(trackingId, encaissementTrackingId, principal.getUsername()));
    }
}