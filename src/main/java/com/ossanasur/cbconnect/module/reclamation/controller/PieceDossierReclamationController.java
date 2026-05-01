package com.ossanasur.cbconnect.module.reclamation.controller;

import com.ossanasur.cbconnect.module.reclamation.dto.request.AssocierDocumentRequest;
import com.ossanasur.cbconnect.module.reclamation.dto.response.MaturiteDossierResponse;
import com.ossanasur.cbconnect.module.reclamation.dto.response.PieceDossierResponse;
import com.ossanasur.cbconnect.module.reclamation.service.PiecesAdministrativesService;
import com.ossanasur.cbconnect.utils.DataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/dossiers")
@RequiredArgsConstructor
@Tag(name = "Pièces dossier", description = "Gestion des pièces administratives par dossier de réclamation")
public class PieceDossierReclamationController {

    private final PiecesAdministrativesService piecesService;

    /**
     * GET /v1/dossiers/{dossierTrackingId}/pieces
     * Retourne la liste des pièces avec statut + indicateur de maturité
     */
    @GetMapping("/{dossierTrackingId}/pieces")
    @Operation(summary = "Checklist des pièces d'un dossier + maturité")
    public ResponseEntity<DataResponse<MaturiteDossierResponse>> getMaturite(
            @PathVariable UUID dossierTrackingId) {
        return ResponseEntity.ok(piecesService.getMaturiteDossier(dossierTrackingId));
    }

    /**
     * POST /v1/dossiers/{dossierTrackingId}/pieces/initialiser
     * Réinitialise les pièces d'un dossier (utile pour les dossiers créés avant la correction du mapping victime→TypeDommage)
     */
    @PostMapping("/{dossierTrackingId}/pieces/initialiser")
    @Operation(summary = "Réinitialiser les pièces administratives d'un dossier")
    public ResponseEntity<DataResponse<Void>> initialiser(
            @PathVariable UUID dossierTrackingId,
            Authentication auth) {
        piecesService.initialiserPiecesDossier(dossierTrackingId, auth.getName());
        return ResponseEntity.ok(DataResponse.success("Pièces réinitialisées", null));
    }

    /**
     * POST /v1/dossiers/pieces/{pieceDossierTrackingId}/associer
     * Associe un document GED à la pièce → statut RECUE
     */
    @PostMapping("/pieces/{pieceDossierTrackingId}/associer")
    @Operation(summary = "Associer un document GED à une pièce")
    public ResponseEntity<DataResponse<PieceDossierResponse>> associer(
            @PathVariable UUID pieceDossierTrackingId,
            @Valid @RequestBody AssocierDocumentRequest req,
            Authentication auth) {
        return ResponseEntity.ok(piecesService.associerDocument(pieceDossierTrackingId, req, auth.getName()));
    }

    /**
     * PATCH /v1/dossiers/pieces/{pieceDossierTrackingId}/rejeter
     * Rejette le document associé → statut REJETEE
     */
    @PatchMapping("/pieces/{pieceDossierTrackingId}/rejeter")
    @Operation(summary = "Rejeter le document associé à une pièce")
    public ResponseEntity<DataResponse<PieceDossierResponse>> rejeter(
            @PathVariable UUID pieceDossierTrackingId,
            @RequestParam(required = false) String notes,
            Authentication auth) {
        return ResponseEntity.ok(piecesService.rejeterDocument(pieceDossierTrackingId, notes, auth.getName()));
    }

    /**
     * PATCH /v1/dossiers/pieces/{pieceDossierTrackingId}/retirer
     * Retire le document → statut repasse à ATTENDUE
     */
    @PatchMapping("/pieces/{pieceDossierTrackingId}/retirer")
    @Operation(summary = "Retirer le document d'une pièce (repasse à ATTENDUE)")
    public ResponseEntity<DataResponse<PieceDossierResponse>> retirer(
            @PathVariable UUID pieceDossierTrackingId,
            Authentication auth) {
        return ResponseEntity.ok(piecesService.retirerDocument(pieceDossierTrackingId, auth.getName()));
    }
}
