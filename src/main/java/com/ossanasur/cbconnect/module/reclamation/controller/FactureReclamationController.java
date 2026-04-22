package com.ossanasur.cbconnect.module.reclamation.controller;

import com.ossanasur.cbconnect.module.reclamation.dto.request.UpdateFactureRequest;
import com.ossanasur.cbconnect.module.reclamation.dto.response.FactureReclamationResponse;
import com.ossanasur.cbconnect.module.reclamation.service.ReclamationService;
import com.ossanasur.cbconnect.utils.DataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/v1/factures-reclamation")
@RequiredArgsConstructor
@Tag(name = "Factures de réclamation",
     description = "Actions sur les factures existantes : validation, rejet, suppression, upload scan")
@SecurityRequirement(name = "bearerAuth")
public class FactureReclamationController {

    private final ReclamationService reclamationService;

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR')")
    @Operation(summary = "Trancher une facture (VALIDE / VALIDE_PARTIELLEMENT / REJETE / EN_ATTENTE)")
    public ResponseEntity<DataResponse<FactureReclamationResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFactureRequest req,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(reclamationService.updateFacture(id, req, u.getUsername()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR')")
    @Operation(summary = "Supprimer une facture (autorisé uniquement en statut EN_ATTENTE)")
    public ResponseEntity<DataResponse<Void>> supprimer(
            @PathVariable UUID id, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(reclamationService.supprimerFacture(id, u.getUsername()));
    }

    @PostMapping(value = "/{id}/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR','SECRETAIRE')")
    @Operation(summary = "Téléverser le scan de la facture (archivé dans la GED, tag FACTURE_RECLAMATION)")
    public ResponseEntity<DataResponse<FactureReclamationResponse>> uploadDocument(
            @PathVariable UUID id,
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "titre", required = false) String titre,
            @RequestPart(value = "typeDocument", required = false) String typeDocument,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(
                reclamationService.attacherDocumentFacture(id, file, titre, typeDocument, u.getUsername()));
    }
}
