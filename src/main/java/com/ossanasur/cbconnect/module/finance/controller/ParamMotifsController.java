package com.ossanasur.cbconnect.module.finance.controller;

import com.ossanasur.cbconnect.common.enums.TypeMotif;
import com.ossanasur.cbconnect.module.finance.dto.request.ParamMotifRequest;
import com.ossanasur.cbconnect.module.finance.dto.response.ParamMotifResponse;
import com.ossanasur.cbconnect.module.finance.service.ParamMotifService;
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
@RequestMapping("/api/v1/param-motifs")
@RequiredArgsConstructor
@Tag(name = "Param Motifs", description = "Paramétrage des motifs de règlement, annulation et préfinancement.")
@SecurityRequirement(name = "bearerAuth")
public class ParamMotifsController {

    private final ParamMotifService paramMotifService;

    /**
     * Liste tous les motifs actifs.
     * - Si ?type= est fourni  → liste filtrée non paginée (DataResponse<List>)
     * - Sinon                  → liste paginée (?page=&size=)
     */
    @GetMapping
    @Operation(summary = "Lister les motifs (filtrés par type ou paginés)")
    public ResponseEntity<?> lister(
            @RequestParam(required = false) TypeMotif type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (type != null) {
            List<ParamMotifResponse> liste = paramMotifService.listerParType(type);
            return ResponseEntity.ok(DataResponse.success(null, liste));
        }
        PaginatedResponse<ParamMotifResponse> paginated = paramMotifService.listerTous(page, size);
        return ResponseEntity.ok(paginated);
    }

    @GetMapping("/{trackingId}")
    @Operation(summary = "Détail d'un motif par trackingId")
    public ResponseEntity<DataResponse<ParamMotifResponse>> getOne(@PathVariable UUID trackingId) {
        return ResponseEntity.ok(DataResponse.success(null, paramMotifService.getByTrackingId(trackingId)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PARAM_MOTIFS_GERER')")
    @Operation(summary = "Créer un motif paramétré")
    public ResponseEntity<DataResponse<ParamMotifResponse>> creer(
            @Valid @RequestBody ParamMotifRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paramMotifService.creer(request, principal.getUsername()));
    }

    @PutMapping("/{trackingId}")
    @PreAuthorize("hasAuthority('PARAM_MOTIFS_GERER')")
    @Operation(summary = "Modifier un motif paramétré")
    public ResponseEntity<DataResponse<ParamMotifResponse>> modifier(
            @PathVariable UUID trackingId,
            @Valid @RequestBody ParamMotifRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(paramMotifService.modifier(trackingId, request, principal.getUsername()));
    }

    @DeleteMapping("/{trackingId}")
    @PreAuthorize("hasAuthority('PARAM_MOTIFS_GERER')")
    @Operation(summary = "Supprimer (soft-delete) un motif paramétré")
    public ResponseEntity<DataResponse<Void>> supprimer(
            @PathVariable UUID trackingId,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(paramMotifService.supprimer(trackingId, principal.getUsername()));
    }
}
