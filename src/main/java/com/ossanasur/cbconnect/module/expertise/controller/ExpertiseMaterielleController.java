package com.ossanasur.cbconnect.module.expertise.controller;

import com.ossanasur.cbconnect.module.expertise.dto.request.ExpertiseMaterielleRequest;
import com.ossanasur.cbconnect.module.expertise.dto.response.ExpertiseMaterielleResponse;
import com.ossanasur.cbconnect.module.expertise.service.impl.ExpertiseMaterielleServiceImpl;
import com.ossanasur.cbconnect.utils.DataResponse;
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
@RequestMapping("/v1/expertises-materielles")
@RequiredArgsConstructor
@Tag(name = "Expertises Matérielles", description = "Rapports d'expertise automobile")
@SecurityRequirement(name = "bearerAuth")
public class ExpertiseMaterielleController {

    private final ExpertiseMaterielleServiceImpl service;

    @PostMapping
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR')")
    public ResponseEntity<DataResponse<ExpertiseMaterielleResponse>> create(
            @Valid @RequestBody ExpertiseMaterielleRequest req,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(service.create(req, u.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DataResponse<ExpertiseMaterielleResponse>> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getByTrackingId(id));
    }

    @GetMapping("/victime/{victimeId}")
    public ResponseEntity<DataResponse<List<ExpertiseMaterielleResponse>>> getByVictime(
            @PathVariable UUID victimeId) {
        return ResponseEntity.ok(service.getByVictime(victimeId));
    }

    @GetMapping("/sinistre/{sinistreId}")
    public ResponseEntity<DataResponse<List<ExpertiseMaterielleResponse>>> getBySinistre(
            @PathVariable UUID sinistreId) {
        return ResponseEntity.ok(service.getBySinistre(sinistreId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR')")
    public ResponseEntity<DataResponse<ExpertiseMaterielleResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ExpertiseMaterielleRequest req,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(service.update(id, req, u.getUsername()));
    }
}
