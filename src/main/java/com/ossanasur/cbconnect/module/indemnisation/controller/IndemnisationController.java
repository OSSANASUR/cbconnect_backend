package com.ossanasur.cbconnect.module.indemnisation.controller;
import com.ossanasur.cbconnect.module.indemnisation.dto.request.*;
import com.ossanasur.cbconnect.module.indemnisation.dto.response.*;
import com.ossanasur.cbconnect.module.indemnisation.service.IndemnisationService;
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
@RestController @RequestMapping("/v1/indemnisation") @RequiredArgsConstructor
@Tag(name="Indemnisation",description="Calcul CIMA art.258-266 et gestion des offres")
@SecurityRequirement(name="bearerAuth")
public class IndemnisationController {
    private final IndemnisationService indemnisationService;
    @PostMapping("/calculer/{victimeId}")
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR')")
    @Operation(summary="Calculer l'offre d'indemnisation CIMA pour une victime blessée")
    public ResponseEntity<DataResponse<OffreIndemnisationResponse>> calculer(
            @PathVariable UUID victimeId, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(indemnisationService.calculerOffreBlesse(victimeId, u.getUsername())); }
    @GetMapping("/offre/victime/{victimeId}")
    @Operation(summary="Obtenir la derniere offre calculee pour une victime")
    public ResponseEntity<DataResponse<OffreIndemnisationResponse>> getOffre(@PathVariable UUID victimeId) {
        return ResponseEntity.ok(indemnisationService.getOffreByVictime(victimeId)); }
    @PatchMapping("/offre/{offreId}/valider")
    @PreAuthorize("hasAnyRole('SE','CSS')")
    @Operation(summary="Valider une offre d'indemnisation (CSS ou SE)")
    public ResponseEntity<DataResponse<OffreIndemnisationResponse>> valider(
            @PathVariable UUID offreId, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(indemnisationService.validerOffre(offreId, u.getUsername())); }
    @GetMapping("/penalites/{offreId}")
    @Operation(summary="Calculer les penalites de retard sur une offre (art.231 CIMA)")
    public ResponseEntity<DataResponse<BigDecimal>> penalites(@PathVariable UUID offreId) {
        return ResponseEntity.ok(indemnisationService.calculerPenalites(offreId)); }
    @PostMapping("/ayants-droit")
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR')")
    @Operation(summary="Ajouter un ayant droit a une victime decedee")
    public ResponseEntity<DataResponse<AyantDroitResponse>> ajouterAyantDroit(
            @Valid @RequestBody AyantDroitRequest r, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(indemnisationService.ajouterAyantDroit(r, u.getUsername())); }
    @GetMapping("/ayants-droit/victime/{victimeId}")
    public ResponseEntity<DataResponse<List<AyantDroitResponse>>> getAyantsDroit(@PathVariable UUID victimeId) {
        return ResponseEntity.ok(indemnisationService.getAyantsDroitByVictime(victimeId)); }
}
