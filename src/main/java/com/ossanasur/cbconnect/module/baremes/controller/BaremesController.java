package com.ossanasur.cbconnect.module.baremes.controller;

import com.ossanasur.cbconnect.module.baremes.dto.request.*;
import com.ossanasur.cbconnect.module.baremes.dto.response.*;
import com.ossanasur.cbconnect.module.baremes.service.BaremesService;
import com.ossanasur.cbconnect.utils.DataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/baremes")
@RequiredArgsConstructor
@Tag(name = "Barèmes CIMA", description = "Paramétrage des tables CIMA art. 258-266")
@SecurityRequirement(name = "bearerAuth")
public class BaremesController {

    private final BaremesService service;

    // ── Capitalisation (art. 260-b) ─────────────────────────────────
    @GetMapping("/capitalisation")
    @Operation(summary = "Lister les barèmes de capitalisation (M100/F100/M25/F25)")
    public ResponseEntity<DataResponse<List<BaremeCapitalisationResponse>>> listCapitalisation() {
        return ResponseEntity.ok(service.listCapitalisation());
    }

    @PostMapping("/capitalisation")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DataResponse<BaremeCapitalisationResponse>> createCapitalisation(
            @Valid @RequestBody BaremeCapitalisationRequest r) {
        return ResponseEntity.ok(service.createCapitalisation(r));
    }

    @PutMapping("/capitalisation/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DataResponse<BaremeCapitalisationResponse>> updateCapitalisation(
            @PathVariable Integer id, @Valid @RequestBody BaremeCapitalisationRequest r) {
        return ResponseEntity.ok(service.updateCapitalisation(id, r));
    }

    @DeleteMapping("/capitalisation/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DataResponse<Void>> deleteCapitalisation(@PathVariable Integer id) {
        return ResponseEntity.ok(service.deleteCapitalisation(id));
    }

    // ── Valeur point IP (art. 260-a) ────────────────────────────────
    @GetMapping("/valeur-point-ip")
    @Operation(summary = "Lister les valeurs du point IP par tranches age/taux")
    public ResponseEntity<DataResponse<List<BaremeValeurPointIpResponse>>> listValeurPointIp() {
        return ResponseEntity.ok(service.listValeurPointIp());
    }

    @PostMapping("/valeur-point-ip")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DataResponse<BaremeValeurPointIpResponse>> createValeurPointIp(
            @Valid @RequestBody BaremeValeurPointIpRequest r) {
        return ResponseEntity.ok(service.createValeurPointIp(r));
    }

    @PutMapping("/valeur-point-ip/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DataResponse<BaremeValeurPointIpResponse>> updateValeurPointIp(
            @PathVariable Integer id, @Valid @RequestBody BaremeValeurPointIpRequest r) {
        return ResponseEntity.ok(service.updateValeurPointIp(id, r));
    }

    @DeleteMapping("/valeur-point-ip/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DataResponse<Void>> deleteValeurPointIp(@PathVariable Integer id) {
        return ResponseEntity.ok(service.deleteValeurPointIp(id));
    }

    // ── Clé répartition (art. 265) ──────────────────────────────────
    @GetMapping("/cle-repartition-265")
    @Operation(summary = "Lister les clés de répartition par situation familiale")
    public ResponseEntity<DataResponse<List<BaremeCleRepartition265Response>>> listCleRepartition() {
        return ResponseEntity.ok(service.listCleRepartition());
    }

    @PostMapping("/cle-repartition-265")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DataResponse<BaremeCleRepartition265Response>> createCleRepartition(
            @Valid @RequestBody BaremeCleRepartition265Request r) {
        return ResponseEntity.ok(service.createCleRepartition(r));
    }

    @PutMapping("/cle-repartition-265/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DataResponse<BaremeCleRepartition265Response>> updateCleRepartition(
            @PathVariable Integer id, @Valid @RequestBody BaremeCleRepartition265Request r) {
        return ResponseEntity.ok(service.updateCleRepartition(id, r));
    }

    @DeleteMapping("/cle-repartition-265/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DataResponse<Void>> deleteCleRepartition(@PathVariable Integer id) {
        return ResponseEntity.ok(service.deleteCleRepartition(id));
    }

    // ── Préjudice moral (art. 266) ──────────────────────────────────
    @GetMapping("/prejudice-moral-266")
    @Operation(summary = "Lister les clés du préjudice moral par lien de parenté")
    public ResponseEntity<DataResponse<List<BaremePrejudiceMoral266Response>>> listPrejudiceMoral() {
        return ResponseEntity.ok(service.listPrejudiceMoral());
    }

    @PostMapping("/prejudice-moral-266")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DataResponse<BaremePrejudiceMoral266Response>> createPrejudiceMoral(
            @Valid @RequestBody BaremePrejudiceMoral266Request r) {
        return ResponseEntity.ok(service.createPrejudiceMoral(r));
    }

    @PutMapping("/prejudice-moral-266/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DataResponse<BaremePrejudiceMoral266Response>> updatePrejudiceMoral(
            @PathVariable Integer id, @Valid @RequestBody BaremePrejudiceMoral266Request r) {
        return ResponseEntity.ok(service.updatePrejudiceMoral(id, r));
    }

    @DeleteMapping("/prejudice-moral-266/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DataResponse<Void>> deletePrejudiceMoral(@PathVariable Integer id) {
        return ResponseEntity.ok(service.deletePrejudiceMoral(id));
    }

    // ── Pretium doloris / préjudice esthétique (art. 262) ───────────
    @GetMapping("/pretium-doloris")
    @Operation(summary = "Lister les qualifications pretium doloris / préjudice esthétique")
    public ResponseEntity<DataResponse<List<BaremePretiumDolorisResponse>>> listPretiumDoloris() {
        return ResponseEntity.ok(service.listPretiumDoloris());
    }

    @PostMapping("/pretium-doloris")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DataResponse<BaremePretiumDolorisResponse>> createPretiumDoloris(
            @Valid @RequestBody BaremePretiumDolorisRequest r) {
        return ResponseEntity.ok(service.createPretiumDoloris(r));
    }

    @PutMapping("/pretium-doloris/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DataResponse<BaremePretiumDolorisResponse>> updatePretiumDoloris(
            @PathVariable Integer id, @Valid @RequestBody BaremePretiumDolorisRequest r) {
        return ResponseEntity.ok(service.updatePretiumDoloris(id, r));
    }

    @DeleteMapping("/pretium-doloris/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DataResponse<Void>> deletePretiumDoloris(@PathVariable Integer id) {
        return ResponseEntity.ok(service.deletePretiumDoloris(id));
    }
}
