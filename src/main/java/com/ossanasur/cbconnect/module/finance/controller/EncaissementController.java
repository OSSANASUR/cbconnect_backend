package com.ossanasur.cbconnect.module.finance.controller;

import com.ossanasur.cbconnect.module.finance.dto.request.EncaissementRequest;
import com.ossanasur.cbconnect.module.finance.dto.response.EncaissementResponse;
import com.ossanasur.cbconnect.module.finance.service.EncaissementService;
import com.ossanasur.cbconnect.utils.DataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/encaissements")
@RequiredArgsConstructor
@Tag(name = "Encaissements", description = "Cheques sinistres recus des bureaux homologues")
@SecurityRequirement(name = "bearerAuth")
public class EncaissementController {
    private final EncaissementService encaissementService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SE','COMPTABLE')")
    @Operation(summary = "Enregistrer un cheque recu")
    public ResponseEntity<DataResponse<EncaissementResponse>> create(@Valid @RequestBody EncaissementRequest r,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(encaissementService.create(r, u.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DataResponse<EncaissementResponse>> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(encaissementService.getByTrackingId(id));
    }

    @GetMapping("/sinistre/{sinistreId}")
    public ResponseEntity<DataResponse<List<EncaissementResponse>>> getBySinistre(@PathVariable UUID sinistreId) {
        return ResponseEntity.ok(encaissementService.getBySinistre(sinistreId));
    }

    @PatchMapping("/{id}/encaisser")
    @PreAuthorize("hasAnyRole('SE','COMPTABLE')")
    @Operation(summary = "Marquer un cheque comme encaisse en banque")
    public ResponseEntity<DataResponse<Void>> encaisser(@PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateEncaissement,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(encaissementService.encaisser(id, dateEncaissement, u.getUsername()));
    }

    @PatchMapping("/{id}/annuler")
    @PreAuthorize("hasRole('SE')")
    @Operation(summary = "Annuler un encaissement (autorisation SE requise)")
    public ResponseEntity<DataResponse<Void>> annuler(@PathVariable UUID id, @RequestParam String motif,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(encaissementService.annuler(id, motif, u.getUsername()));
    }
}
