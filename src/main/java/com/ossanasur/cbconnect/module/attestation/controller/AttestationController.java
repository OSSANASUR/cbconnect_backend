package com.ossanasur.cbconnect.module.attestation.controller;
import com.ossanasur.cbconnect.module.attestation.dto.request.*;
import com.ossanasur.cbconnect.module.attestation.dto.response.*;
import com.ossanasur.cbconnect.module.attestation.service.AttestationService;
import com.ossanasur.cbconnect.utils.DataResponse;
import com.ossanasur.cbconnect.utils.PaginatedResponse;
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
import java.time.LocalDate; import java.util.UUID;
@RestController @RequestMapping("/v1/attestations") @RequiredArgsConstructor
@Tag(name="Attestations Carte Brune",description="Commandes, facturation et livraison des attestations")
@SecurityRequirement(name="bearerAuth")
public class AttestationController {
    private final AttestationService attestationService;
    @PostMapping("/commandes")
    @PreAuthorize("hasAnyRole('SE','CSS','COMPTABLE')")
    @Operation(summary="Passer une commande d'attestations pour une compagnie membre")
    public ResponseEntity<DataResponse<CommandeAttestationResponse>> commander(
            @Valid @RequestBody CommandeAttestationRequest r, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(attestationService.passerCommande(r, u.getUsername())); }
    @GetMapping("/commandes") @Operation(summary="Lister toutes les commandes")
    public ResponseEntity<PaginatedResponse<CommandeAttestationResponse>> getAll(
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        return ResponseEntity.ok(attestationService.getAllCommandes(page, size)); }
    @GetMapping("/commandes/{id}")
    public ResponseEntity<DataResponse<CommandeAttestationResponse>> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(attestationService.getCommande(id)); }
    @GetMapping("/commandes/organisme/{orgId}")
    public ResponseEntity<PaginatedResponse<CommandeAttestationResponse>> getByOrganisme(
            @PathVariable UUID orgId, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        return ResponseEntity.ok(attestationService.getCommandesByOrganisme(orgId, page, size)); }
    @PostMapping("/commandes/{commandeId}/proforma")
    @PreAuthorize("hasAnyRole('SE','COMPTABLE')")
    @Operation(summary="Generer la facture proforma")
    public ResponseEntity<DataResponse<FactureAttestationResponse>> proforma(
            @PathVariable UUID commandeId, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(attestationService.genererProforma(commandeId, u.getUsername())); }
    @PostMapping("/commandes/{commandeId}/facture")
    @PreAuthorize("hasAnyRole('SE','COMPTABLE')")
    @Operation(summary="Generer la facture definitive")
    public ResponseEntity<DataResponse<FactureAttestationResponse>> facture(
            @PathVariable UUID commandeId, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(attestationService.genererFactureDefinitive(commandeId, u.getUsername())); }
    @PatchMapping("/commandes/{commandeId}/livrer")
    @PreAuthorize("hasAnyRole('SE','COMPTABLE')")
    @Operation(summary="Marquer une commande comme livree")
    public ResponseEntity<DataResponse<Void>> livrer(@PathVariable UUID commandeId,
            @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate dateLivraison,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(attestationService.marquerLivre(commandeId, dateLivraison, u.getUsername())); }
    @PatchMapping("/commandes/{commandeId}/annuler")
    @PreAuthorize("hasRole('SE')")
    public ResponseEntity<DataResponse<Void>> annuler(@PathVariable UUID commandeId, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(attestationService.annulerCommande(commandeId, u.getUsername())); }
}
