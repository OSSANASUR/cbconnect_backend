package com.ossanasur.cbconnect.module.reclamation.controller;
import com.ossanasur.cbconnect.module.reclamation.dto.request.*;
import com.ossanasur.cbconnect.module.reclamation.dto.response.*;
import com.ossanasur.cbconnect.module.reclamation.service.ReclamationService;
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
import java.math.BigDecimal; import java.util.List; import java.util.UUID;
@RestController @RequestMapping("/v1/reclamations") @RequiredArgsConstructor
@Tag(name="Pieces de Reclamation",description="Gestion des factures de remboursement des victimes")
@SecurityRequirement(name="bearerAuth")
public class ReclamationController {
    private final ReclamationService reclamationService;
    @PostMapping("/dossiers") @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR')")
    @Operation(summary="Ouvrir un dossier de reclamation pour une victime")
    public ResponseEntity<DataResponse<DossierReclamationResponse>> ouvrirDossier(@Valid @RequestBody DossierReclamationRequest r, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(reclamationService.ouvrirDossier(r, u.getUsername())); }
    @GetMapping("/dossiers/{id}")
    public ResponseEntity<DataResponse<DossierReclamationResponse>> getDossier(@PathVariable UUID id) {
        return ResponseEntity.ok(reclamationService.getDossier(id)); }
    @GetMapping("/dossiers/victime/{victimeId}")
    public ResponseEntity<DataResponse<List<DossierReclamationResponse>>> getByVictime(@PathVariable UUID victimeId) {
        return ResponseEntity.ok(reclamationService.getDossiersByVictime(victimeId)); }
    @PostMapping("/factures") @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR')")
    @Operation(summary="Ajouter une facture a un dossier")
    public ResponseEntity<DataResponse<FactureReclamationResponse>> ajouterFacture(@Valid @RequestBody FactureReclamationRequest r, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(reclamationService.ajouterFacture(r, u.getUsername())); }
    @PatchMapping("/factures/{id}/valider") @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR')")
    @Operation(summary="Valider une facture avec le montant retenu")
    public ResponseEntity<DataResponse<FactureReclamationResponse>> valider(@PathVariable UUID id, @RequestParam BigDecimal montantRetenu, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(reclamationService.validerFacture(id, montantRetenu, u.getUsername())); }
    @PatchMapping("/factures/{id}/rejeter") @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR')")
    public ResponseEntity<DataResponse<FactureReclamationResponse>> rejeter(@PathVariable UUID id, @RequestParam String motif, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(reclamationService.rejeterFacture(id, motif, u.getUsername())); }
    @GetMapping("/factures/dossier/{dossierId}")
    public ResponseEntity<DataResponse<List<FactureReclamationResponse>>> getByDossier(@PathVariable UUID dossierId) {
        return ResponseEntity.ok(reclamationService.getFacturesByDossier(dossierId)); }
    @PatchMapping("/dossiers/{id}/cloturer") @PreAuthorize("hasAnyRole('SE','CSS')")
    @Operation(summary="Cloturer un dossier de reclamation")
    public ResponseEntity<DataResponse<Void>> cloturer(@PathVariable UUID id, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(reclamationService.clotureDossier(id, u.getUsername())); }
}
