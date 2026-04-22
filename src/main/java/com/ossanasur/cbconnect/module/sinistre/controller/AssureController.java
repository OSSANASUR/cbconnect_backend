package com.ossanasur.cbconnect.module.sinistre.controller;

import com.ossanasur.cbconnect.module.sinistre.dto.request.AssureRequest;
import com.ossanasur.cbconnect.module.sinistre.dto.response.AssureResponse;
import com.ossanasur.cbconnect.module.sinistre.service.AssureService;
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

import java.util.UUID;

@RestController
@RequestMapping("/v1/assures")
@RequiredArgsConstructor
@Tag(name = "Assures", description = "Titulaires du contrat d'assurance Carte Brune")
@SecurityRequirement(name = "bearerAuth")
public class AssureController {

    private final AssureService assureService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR','SECRETAIRE')")
    @Operation(summary = "Enregistrer un assure")
    public ResponseEntity<DataResponse<AssureResponse>> create(@Valid @RequestBody AssureRequest r,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(assureService.create(r, u.getUsername()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un assure par trackingId")
    public ResponseEntity<DataResponse<AssureResponse>> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(assureService.getByTrackingId(id));
    }

    @GetMapping("/attestation/{numero}")
    @Operation(summary = "Lookup d'un assure par numero d'attestation")
    public ResponseEntity<DataResponse<AssureResponse>> getByAttestation(@PathVariable String numero) {
        return ResponseEntity.ok(assureService.getByNumeroAttestation(numero));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR')")
    @Operation(summary = "Modifier un assure")
    public ResponseEntity<DataResponse<AssureResponse>> update(@PathVariable UUID id,
            @Valid @RequestBody AssureRequest r, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(assureService.update(id, r, u.getUsername()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SE')")
    @Operation(summary = "Supprimer logiquement un assure")
    public ResponseEntity<DataResponse<Void>> delete(@PathVariable UUID id,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(assureService.delete(id, u.getUsername()));
    }
}
