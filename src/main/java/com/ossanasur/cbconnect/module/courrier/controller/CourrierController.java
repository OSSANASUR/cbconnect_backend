package com.ossanasur.cbconnect.module.courrier.controller;
import com.ossanasur.cbconnect.module.courrier.dto.request.CourrierRequest;
import com.ossanasur.cbconnect.module.courrier.dto.response.CourrierResponse;
import com.ossanasur.cbconnect.module.courrier.service.CourrierService;
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
import java.util.List; import java.util.UUID;
@RestController @RequestMapping("/v1/courriers") @RequiredArgsConstructor
@Tag(name="Courriers",description="Correspondances entrantes et sortantes du BNCB")
@SecurityRequirement(name="bearerAuth")
public class CourrierController {
    private final CourrierService courrierService;
    @PostMapping @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR','SECRETAIRE')")
    @Operation(summary="Enregistrer un courrier entrant ou sortant")
    public ResponseEntity<DataResponse<CourrierResponse>> enregistrer(@Valid @RequestBody CourrierRequest r, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(courrierService.enregistrer(r, u.getUsername())); }
    @GetMapping("/{id}") public ResponseEntity<DataResponse<CourrierResponse>> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(courrierService.getByTrackingId(id)); }
    @GetMapping("/sinistre/{sinistreId}") public ResponseEntity<DataResponse<List<CourrierResponse>>> getBySinistre(@PathVariable UUID sinistreId) {
        return ResponseEntity.ok(courrierService.getBySinistre(sinistreId)); }
    @GetMapping("/non-traites") @PreAuthorize("hasAnyRole('SE','CSS','SECRETAIRE')")
    @Operation(summary="Courriers en attente de traitement")
    public ResponseEntity<DataResponse<List<CourrierResponse>>> getNonTraites() {
        return ResponseEntity.ok(courrierService.getNonTraites()); }
    @PatchMapping("/{id}/traiter") @PreAuthorize("hasAnyRole('SE','CSS','SECRETAIRE','REDACTEUR')")
    public ResponseEntity<DataResponse<Void>> traiter(@PathVariable UUID id, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(courrierService.marquerTraite(id, u.getUsername())); }
    @DeleteMapping("/{id}") @PreAuthorize("hasAnyRole('SE','CSS')")
    public ResponseEntity<DataResponse<Void>> supprimer(@PathVariable UUID id, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(courrierService.supprimer(id, u.getUsername())); }
}
