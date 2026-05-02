package com.ossanasur.cbconnect.module.finance.controller;

import com.ossanasur.cbconnect.module.finance.dto.request.AdminReconciliationRequest;
import com.ossanasur.cbconnect.module.finance.service.PaiementImputationService;
import com.ossanasur.cbconnect.utils.DataResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/admin/reconciliation")
@RequiredArgsConstructor
public class AdminReconciliationController {

    private final PaiementImputationService imputationService;

    @PostMapping("/sinistres/{sinistreTrackingId}")
    @Operation(summary = "Backfill manuel des imputations historiques pour un sinistre (réservé COMPTABLE/ADMIN)")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPTABLE')")
    public ResponseEntity<DataResponse<Integer>> reconcilier(
            @PathVariable UUID sinistreTrackingId,
            @Valid @RequestBody AdminReconciliationRequest request,
            @AuthenticationPrincipal UserDetails user) {
        int nbCreated = imputationService.backfillImputations(
                sinistreTrackingId,
                request.paiementsImputations(),
                user != null ? user.getUsername() : null);
        return ResponseEntity.ok(DataResponse.success(
                nbCreated + " imputations créées", nbCreated));
    }
}
