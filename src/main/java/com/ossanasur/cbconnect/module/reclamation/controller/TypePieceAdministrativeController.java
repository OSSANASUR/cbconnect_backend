package com.ossanasur.cbconnect.module.reclamation.controller;

import com.ossanasur.cbconnect.module.reclamation.dto.request.TypePieceRequest;
import com.ossanasur.cbconnect.module.reclamation.dto.response.TypePieceResponse;
import com.ossanasur.cbconnect.module.reclamation.service.PiecesAdministrativesService;
import com.ossanasur.cbconnect.utils.DataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/parametrage/pieces")
@RequiredArgsConstructor
@Tag(name = "Paramétrage pièces", description = "Gestion des types de pièces administratives par type de dommage")
public class TypePieceAdministrativeController {

    private final PiecesAdministrativesService piecesService;

    @GetMapping
    @Operation(summary = "Liste tous les types de pièces actifs")
    public ResponseEntity<DataResponse<List<TypePieceResponse>>> lister() {
        return ResponseEntity.ok(piecesService.listerTypesPieces());
    }

    @PostMapping
    @Operation(summary = "Créer un type de pièce administrative")
    public ResponseEntity<DataResponse<TypePieceResponse>> creer(
            @Valid @RequestBody TypePieceRequest req,
            Authentication auth) {
        return ResponseEntity.ok(piecesService.creerTypePiece(req, auth.getName()));
    }

    @PutMapping("/{trackingId}")
    @Operation(summary = "Modifier un type de pièce")
    public ResponseEntity<DataResponse<TypePieceResponse>> modifier(
            @PathVariable UUID trackingId,
            @Valid @RequestBody TypePieceRequest req,
            Authentication auth) {
        return ResponseEntity.ok(piecesService.modifierTypePiece(trackingId, req, auth.getName()));
    }

    @PatchMapping("/{trackingId}/toggle")
    @Operation(summary = "Activer / désactiver un type de pièce")
    public ResponseEntity<DataResponse<Void>> toggle(
            @PathVariable UUID trackingId,
            Authentication auth) {
        return ResponseEntity.ok(piecesService.toggleActif(trackingId, auth.getName()));
    }
}
