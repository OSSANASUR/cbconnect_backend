package com.ossanasur.cbconnect.module.attestation.controller;
import com.ossanasur.cbconnect.module.attestation.dto.request.*;
import com.ossanasur.cbconnect.module.attestation.dto.response.*;
import com.ossanasur.cbconnect.module.attestation.service.AttestationService;
import com.ossanasur.cbconnect.module.attestation.service.FacturePdfService;
import com.ossanasur.cbconnect.module.attestation.service.impl.AttestationServiceImpl;
import com.ossanasur.cbconnect.utils.DataResponse;
import com.ossanasur.cbconnect.utils.PaginatedResponse;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.Map;
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
import java.time.LocalDate; import java.util.List; import java.util.UUID;
@RestController @RequestMapping("/v1/attestations") @RequiredArgsConstructor
@Tag(name="Attestations Carte Brune",description="Commandes, facturation et livraison des attestations")
@SecurityRequirement(name="bearerAuth")
public class AttestationController {
    private final AttestationService attestationService;
    private final FacturePdfService facturePdfService;
    private final AttestationServiceImpl attestationServiceImpl;

    // ============== COMMANDES ==============
    @PostMapping("/commandes")
    @PreAuthorize("hasAnyRole('SE','CSS','COMPTABLE')")
    @Operation(summary="Passer une commande d'attestations pour une compagnie membre")
    public ResponseEntity<DataResponse<CommandeAttestationResponse>> commander(
            @Valid @RequestBody CommandeAttestationRequest r, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(attestationService.passerCommande(r, u.getUsername())); }

    @GetMapping("/commandes") @Operation(summary="Lister toutes les commandes")
    public ResponseEntity<PaginatedResponse<CommandeAttestationResponse>> getAllCommandes(
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        return ResponseEntity.ok(attestationService.getAllCommandes(page, size)); }

    @GetMapping("/commandes/{id}")
    public ResponseEntity<DataResponse<CommandeAttestationResponse>> getOneCommande(@PathVariable UUID id) {
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
    public ResponseEntity<DataResponse<Void>> annulerCommande(@PathVariable UUID commandeId, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(attestationService.annulerCommande(commandeId, u.getUsername())); }

    @PatchMapping("/commandes/{commandeId}/solder")
    @PreAuthorize("hasAnyRole('SE','COMPTABLE')")
    @Operation(summary="Solder une commande (apres encaissement)")
    public ResponseEntity<DataResponse<Void>> solder(@PathVariable UUID commandeId, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(attestationService.solderCommande(commandeId, u.getUsername())); }

    // ============== LOTS ==============
    @PostMapping("/lots")
    @PreAuthorize("hasAnyRole('SE','COMPTABLE')")
    @Operation(summary="Enregistrer un lot d'approvisionnement")
    public ResponseEntity<DataResponse<LotResponse>> creerLot(
            @Valid @RequestBody LotRequest r, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(attestationService.creerLot(r, u.getUsername())); }

    @GetMapping("/lots")
    @Operation(summary="Lister tous les lots")
    public ResponseEntity<PaginatedResponse<LotResponse>> getAllLots(
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        return ResponseEntity.ok(attestationService.getAllLots(page, size)); }

    @GetMapping("/lots/disponibles")
    @Operation(summary="Lister les lots disponibles (avec stock restant)")
    public ResponseEntity<DataResponse<List<LotResponse>>> getLotsDisponibles() {
        return ResponseEntity.ok(attestationService.getLotsDisponibles()); }

    @GetMapping("/lots/{lotId}")
    public ResponseEntity<DataResponse<LotResponse>> getLot(@PathVariable UUID lotId) {
        return ResponseEntity.ok(attestationService.getLot(lotId)); }

    @PutMapping("/lots/{lotId}")
    @PreAuthorize("hasAnyRole('SE','COMPTABLE')")
    @Operation(summary="Modifier un lot d'approvisionnement")
    public ResponseEntity<DataResponse<LotResponse>> modifierLot(
            @PathVariable UUID lotId, @Valid @RequestBody LotRequest r, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(attestationService.modifierLot(lotId, r, u.getUsername())); }

    // ============== CHÈQUES ==============
    @PostMapping("/cheques")
    @PreAuthorize("hasAnyRole('SE','CSS','COMPTABLE')")
    @Operation(summary="Enregistrer la reception d'un cheque sur une facture")
    public ResponseEntity<DataResponse<ChequeResponse>> enregistrerCheque(
            @Valid @RequestBody ChequeRequest r, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(attestationService.enregistrerCheque(r, u.getUsername())); }

    @GetMapping("/cheques")
    public ResponseEntity<PaginatedResponse<ChequeResponse>> getAllCheques(
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        return ResponseEntity.ok(attestationService.getAllCheques(page, size)); }

    @GetMapping("/cheques/{chequeId}")
    public ResponseEntity<DataResponse<ChequeResponse>> getCheque(@PathVariable UUID chequeId) {
        return ResponseEntity.ok(attestationService.getCheque(chequeId)); }

    @GetMapping("/commandes/{commandeId}/cheques")
    public ResponseEntity<DataResponse<List<ChequeResponse>>> getChequesByCommande(@PathVariable UUID commandeId) {
        return ResponseEntity.ok(attestationService.getChequesByCommande(commandeId)); }

    @PatchMapping("/cheques/{chequeId}/encaisser")
    @PreAuthorize("hasAnyRole('SE','COMPTABLE')")
    @Operation(summary="Marquer un cheque comme encaisse (solde la commande si applicable)")
    public ResponseEntity<DataResponse<Void>> encaisserCheque(@PathVariable UUID chequeId,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate dateEncaissement,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(attestationService.encaisserCheque(chequeId, dateEncaissement, u.getUsername())); }

    @PatchMapping("/cheques/{chequeId}/annuler")
    @PreAuthorize("hasRole('SE')")
    public ResponseEntity<DataResponse<Void>> annulerCheque(@PathVariable UUID chequeId,
            @RequestParam(required=false) String motif, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(attestationService.annulerCheque(chequeId, motif, u.getUsername())); }

    // ============== FACTURES ==============
    @GetMapping("/factures")
    public ResponseEntity<PaginatedResponse<FactureAttestationResponse>> getAllFactures(
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        return ResponseEntity.ok(attestationService.getAllFactures(page, size)); }

    @GetMapping("/factures/{factureId}")
    public ResponseEntity<DataResponse<FactureAttestationResponse>> getFacture(@PathVariable UUID factureId) {
        return ResponseEntity.ok(attestationService.getFacture(factureId)); }

    @GetMapping("/commandes/{commandeId}/factures")
    public ResponseEntity<DataResponse<List<FactureAttestationResponse>>> getFacturesByCommande(@PathVariable UUID commandeId) {
        return ResponseEntity.ok(attestationService.getFacturesByCommande(commandeId)); }

    @GetMapping("/factures/{factureId}/pdf")
    @Operation(summary="Télécharger le PDF de la facture (proforma ou définitive)")
    public ResponseEntity<ByteArrayResource> getFacturePdf(@PathVariable UUID factureId) {
        byte[] pdf = facturePdfService.genererPdf(factureId);
        String filename = facturePdfService.nomFichier(factureId);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
            .contentType(MediaType.APPLICATION_PDF)
            .contentLength(pdf.length)
            .body(new ByteArrayResource(pdf));
    }

    @GetMapping("/parametres")
    @Operation(summary="Paramètres tarifaires courants (lus depuis la table parametre)")
    public ResponseEntity<DataResponse<Map<String, Object>>> getParametresTarifaires() {
        return ResponseEntity.ok(DataResponse.success("Paramètres tarifaires", Map.of(
            "prixUnitaireAttestation", attestationServiceImpl.getPrixUnitaire(),
            "contributionFonds", attestationServiceImpl.getContributionFonds()
        )));
    }

    // ============== TRANCHES DE LIVRAISON ==============
    @PostMapping("/commandes/{commandeId}/tranches")
    @PreAuthorize("hasAnyRole('SE','COMPTABLE')")
    @Operation(summary="Enregistrer une tranche de livraison physique d'attestations")
    public ResponseEntity<DataResponse<TrancheLivraisonResponse>> livrerTranche(
            @PathVariable UUID commandeId, @Valid @RequestBody TrancheLivraisonRequest r,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(attestationService.livrerTranche(commandeId, r, u.getUsername())); }

    @GetMapping("/commandes/{commandeId}/tranches")
    public ResponseEntity<DataResponse<List<TrancheLivraisonResponse>>> getTranches(@PathVariable UUID commandeId) {
        return ResponseEntity.ok(attestationService.getTranchesByCommande(commandeId)); }
}
