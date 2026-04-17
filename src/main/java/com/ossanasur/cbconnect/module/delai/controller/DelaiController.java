package com.ossanasur.cbconnect.module.delai.controller;
import com.ossanasur.cbconnect.module.delai.dto.response.NotificationDelaiResponse;
import com.ossanasur.cbconnect.module.delai.service.DelaiService;
import com.ossanasur.cbconnect.utils.DataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List; import java.util.UUID;
@RestController @RequestMapping("/v1/delais") @RequiredArgsConstructor
@Tag(name="Délais & Alertes",description="Suivi des 37 délais CEDEAO + 12 délais internes")
@SecurityRequirement(name="bearerAuth")
public class DelaiController {
    private final DelaiService delaiService;
    @GetMapping("/sinistre/{sinistreId}")
    @Operation(summary="Lister tous les suivis de délais actifs d'un sinistre")
    public ResponseEntity<DataResponse<List<NotificationDelaiResponse>>> getBySinistre(@PathVariable UUID sinistreId) {
        return ResponseEntity.ok(delaiService.getActiveBySinistre(sinistreId)); }
    @GetMapping("/urgents") @PreAuthorize("hasAnyRole('SE','CSS')")
    @Operation(summary="Dossiers en alerte URGENT ou CRITIQUE — tableau de bord prioritaire")
    public ResponseEntity<DataResponse<List<NotificationDelaiResponse>>> getUrgents() {
        return ResponseEntity.ok(delaiService.getUrgents()); }
    @GetMapping("/mes-alertes")
    @Operation(summary="Mes propres alertes de délais (redacteur connecté)")
    public ResponseEntity<DataResponse<List<NotificationDelaiResponse>>> getMesAlertes(@AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(delaiService.getMesAlertes(u.getUsername())); }
    @PostMapping("/sinistre/{sinistreId}/initialiser") @PreAuthorize("hasAnyRole('SE','CSS')")
    @Operation(summary="Initialiser manuellement les suivis de délais d'un sinistre")
    public ResponseEntity<DataResponse<Void>> initialiser(@PathVariable UUID sinistreId, @AuthenticationPrincipal UserDetails u) {
        delaiService.initialiserDelaisPourSinistre(sinistreId, u.getUsername());
        return ResponseEntity.ok(DataResponse.success("Délais initialisés", null)); }
    @PatchMapping("/{id}/resoudre") @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR')")
    @Operation(summary="Marquer un suivi de délai comme résolu")
    public ResponseEntity<DataResponse<Void>> resoudre(@PathVariable Integer id, @RequestParam String motif, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(delaiService.resoudre(id, motif, u.getUsername())); }
}
