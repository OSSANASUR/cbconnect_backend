package com.ossanasur.cbconnect.module.comptabilite.controller;
import com.ossanasur.cbconnect.module.comptabilite.dto.response.EcritureResponse;
import com.ossanasur.cbconnect.module.comptabilite.service.ComptabiliteService;
import com.ossanasur.cbconnect.utils.DataResponse;
import com.ossanasur.cbconnect.utils.PaginatedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate; import java.util.List; import java.util.UUID;
@RestController @RequestMapping("/v1/comptabilite") @RequiredArgsConstructor
@Tag(name="Comptabilité",description="Ecritures comptables automatiques - integration Perfecto")
@SecurityRequirement(name="bearerAuth")
public class ComptabiliteController {
    private final ComptabiliteService comptabiliteService;
    @PatchMapping("/ecritures/{id}/valider")
    @PreAuthorize("hasAnyRole('SE','COMPTABLE')")
    @Operation(summary="Valider une écriture comptable")
    public ResponseEntity<DataResponse<EcritureResponse>> valider(
            @PathVariable UUID id, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(comptabiliteService.validerEcriture(id, u.getUsername())); }
    @GetMapping("/ecritures/sinistre/{sinistreId}")
    @Operation(summary="Lister les écritures d'un sinistre")
    public ResponseEntity<DataResponse<List<EcritureResponse>>> getBySinistre(@PathVariable UUID sinistreId) {
        return ResponseEntity.ok(comptabiliteService.getBySinistre(sinistreId)); }
    @GetMapping("/ecritures/periode")
    @PreAuthorize("hasAnyRole('SE','COMPTABLE')")
    @Operation(summary="Journal comptable par période")
    public ResponseEntity<PaginatedResponse<EcritureResponse>> getByPeriode(
            @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate fin,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        return ResponseEntity.ok(comptabiliteService.getByPeriode(debut, fin, page, size)); }
}
