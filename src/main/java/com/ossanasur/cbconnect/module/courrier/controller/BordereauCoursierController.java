package com.ossanasur.cbconnect.module.courrier.controller;

import com.ossanasur.cbconnect.common.enums.StatutBordereau;
import com.ossanasur.cbconnect.module.courrier.dto.request.BordereauCoursierRequest;
import com.ossanasur.cbconnect.module.courrier.dto.request.ConfirmerDechargeRequest;
import com.ossanasur.cbconnect.module.courrier.dto.request.MarquerRemisTransporteurRequest;
import com.ossanasur.cbconnect.module.courrier.dto.response.BordereauCoursierResponse;
import com.ossanasur.cbconnect.module.courrier.dto.response.CourrierResponse;
import com.ossanasur.cbconnect.module.courrier.service.BordereauCoursierService;
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
@RequestMapping("/v1/bordereaux")
@RequiredArgsConstructor
@Tag(name = "Bordereaux coursier", description = "Transmission physique des courriers")
@SecurityRequirement(name = "bearerAuth")
public class BordereauCoursierController {

    private final BordereauCoursierService service;

    // ─── Création / modification ─────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasAnyRole('SE','CSS','SECRETAIRE','COURSIER')")
    @Operation(summary = "Créer un bordereau de transmission")
    public ResponseEntity<DataResponse<BordereauCoursierResponse>> creer(
            @Valid @RequestBody BordereauCoursierRequest req,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(service.creer(req, user.getUsername()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SE','CSS','SECRETAIRE','COURSIER')")
    @Operation(summary = "Modifier un bordereau BROUILLON")
    public ResponseEntity<DataResponse<BordereauCoursierResponse>> modifier(
            @PathVariable UUID id,
            @Valid @RequestBody BordereauCoursierRequest req,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(service.modifier(id, req, user.getUsername()));
    }

    // ─── Lecture ──────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'un bordereau")
    public ResponseEntity<DataResponse<BordereauCoursierResponse>> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getByTrackingId(id));
    }

    @GetMapping
    @Operation(summary = "Lister tous les bordereaux")
    public ResponseEntity<DataResponse<List<BordereauCoursierResponse>>> getAll(
            @RequestParam(required = false) StatutBordereau statut) {
        return ResponseEntity.ok(statut != null ? service.getByStatut(statut) : service.getAll());
    }

    @GetMapping("/courriers-prets-a-embarquer")
    @Operation(summary = "Courriers SORTANTS physiques non encore embarqués, optionnellement filtrés par destinataire")
    public ResponseEntity<DataResponse<List<CourrierResponse>>> courriersPretsAEmbarquer(
            @RequestParam(name = "destinataire", required = false) UUID destinataireOrganismeTrackingId) {
        return ResponseEntity.ok(service.getCourriersPretsAEmbarquer(destinataireOrganismeTrackingId));
    }

    // ─── Machine d'état ───────────────────────────────────────────────────

    @PatchMapping("/{id}/imprimer")
    @PreAuthorize("hasAnyRole('SE','CSS','SECRETAIRE','COURSIER')")
    @Operation(summary = "Marquer le bordereau comme imprimé et remis au coursier")
    public ResponseEntity<DataResponse<BordereauCoursierResponse>> imprimer(
            @PathVariable UUID id, @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(service.marquerImprime(id, user.getUsername()));
    }

    @PatchMapping("/{id}/remis-transporteur")
    @PreAuthorize("hasAnyRole('SE','CSS','SECRETAIRE','COURSIER')")
    @Operation(summary = "Confirmer le dépôt à la poste / bus / DHL (avec facture)")
    public ResponseEntity<DataResponse<BordereauCoursierResponse>> remisTransporteur(
            @PathVariable UUID id,
            @Valid @RequestBody MarquerRemisTransporteurRequest req,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(service.marquerRemisTransporteur(id, req, user.getUsername()));
    }

    @PatchMapping("/{id}/decharge-recue")
    @PreAuthorize("hasAnyRole('SE','CSS','SECRETAIRE','COURSIER')")
    @Operation(summary = "Confirmer réception de la décharge signée de l'homologue")
    public ResponseEntity<DataResponse<BordereauCoursierResponse>> dechargeRecue(
            @PathVariable UUID id,
            @Valid @RequestBody ConfirmerDechargeRequest req,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(service.confirmerDechargeRecue(id, req, user.getUsername()));
    }

    @PatchMapping("/{id}/retourner")
    @PreAuthorize("hasAnyRole('SE','CSS')")
    @Operation(summary = "Marquer le bordereau comme retourné (exception)")
    public ResponseEntity<DataResponse<BordereauCoursierResponse>> retourner(
            @PathVariable UUID id,
            @RequestBody(required = false) java.util.Map<String, String> body,
            @AuthenticationPrincipal UserDetails user) {
        String motif = body != null ? body.get("motif") : null;
        return ResponseEntity.ok(service.marquerRetourne(id, motif, user.getUsername()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SE','CSS')")
    @Operation(summary = "Supprimer un bordereau BROUILLON")
    public ResponseEntity<DataResponse<Void>> supprimer(
            @PathVariable UUID id, @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(service.supprimer(id, user.getUsername()));
    }

    // ─── Lignes du bordereau ──────────────────────────────────────────────

    @PostMapping("/{id}/courriers/{courrierId}")
    @PreAuthorize("hasAnyRole('SE','CSS','SECRETAIRE','COURSIER')")
    @Operation(summary = "Ajouter un courrier à un bordereau BROUILLON")
    public ResponseEntity<DataResponse<BordereauCoursierResponse>> ajouterCourrier(
            @PathVariable UUID id,
            @PathVariable UUID courrierId,
            @RequestParam(required = false) Integer ordre,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(service.ajouterCourrier(id, courrierId, ordre, user.getUsername()));
    }

    @DeleteMapping("/{id}/courriers/{courrierId}")
    @PreAuthorize("hasAnyRole('SE','CSS','SECRETAIRE','COURSIER')")
    @Operation(summary = "Retirer un courrier d'un bordereau BROUILLON")
    public ResponseEntity<DataResponse<BordereauCoursierResponse>> retirerCourrier(
            @PathVariable UUID id,
            @PathVariable UUID courrierId,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(service.retirerCourrier(id, courrierId, user.getUsername()));
    }
}
