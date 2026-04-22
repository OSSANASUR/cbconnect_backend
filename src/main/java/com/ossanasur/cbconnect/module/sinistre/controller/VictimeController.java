package com.ossanasur.cbconnect.module.sinistre.controller;

import com.ossanasur.cbconnect.module.sinistre.dto.request.ActionRcRequest;
import com.ossanasur.cbconnect.module.sinistre.dto.request.VictimeRequest;
import com.ossanasur.cbconnect.module.sinistre.dto.response.VictimeResponse;
import com.ossanasur.cbconnect.module.sinistre.service.VictimeService;
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
@RequestMapping("/v1/victimes")
@RequiredArgsConstructor
@Tag(name = "Victimes", description = "Personnes blesses ou decedes dans un sinistre")
@SecurityRequirement(name = "bearerAuth")
public class VictimeController {
    private final VictimeService victimeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR')")
    public ResponseEntity<DataResponse<VictimeResponse>> create(@Valid @RequestBody VictimeRequest r,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(victimeService.create(r, u.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DataResponse<VictimeResponse>> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(victimeService.getByTrackingId(id));
    }

    @GetMapping("/sinistre/{sinistreId}")
    public ResponseEntity<DataResponse<List<VictimeResponse>>> getBySinistre(@PathVariable UUID sinistreId) {
        return ResponseEntity.ok(victimeService.getBySinistre(sinistreId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR')")
    public ResponseEntity<DataResponse<VictimeResponse>> update(@PathVariable UUID id,
            @Valid @RequestBody VictimeRequest r, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(victimeService.update(id, r, u.getUsername()));
    }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR')")
    @Operation(summary = "Changer le statut individuel d'une victime")
    public ResponseEntity<DataResponse<Void>> changerStatut(@PathVariable UUID id, @RequestParam String statut,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(victimeService.changerStatutVictime(id, statut, u.getUsername()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SE','CSS')")
    public ResponseEntity<DataResponse<Void>> delete(@PathVariable UUID id, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(victimeService.delete(id, u.getUsername()));
    }

    @PatchMapping("/{id}/position-rc")
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR','GESTIONNAIRE')")
    @Operation(summary = "Negocier la position RC d'un adversaire (PROPOSER / REJETER / ACCEPTER / TRANCHER)")
    public ResponseEntity<DataResponse<VictimeResponse>> executerActionRc(
            @PathVariable UUID id,
            @Valid @RequestBody ActionRcRequest r,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(victimeService.executerActionRc(id, r, u.getUsername()));
    }
}
