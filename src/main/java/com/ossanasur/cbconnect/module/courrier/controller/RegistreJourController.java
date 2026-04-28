package com.ossanasur.cbconnect.module.courrier.controller;

import com.ossanasur.cbconnect.common.enums.TypeRegistre;
import com.ossanasur.cbconnect.module.courrier.dto.request.RegistreJourRequest;
import com.ossanasur.cbconnect.module.courrier.dto.request.VisaRegistreRequest;
import com.ossanasur.cbconnect.module.courrier.dto.response.CourrierResponse;
import com.ossanasur.cbconnect.module.courrier.dto.response.RegistreJourResponse;
import com.ossanasur.cbconnect.module.courrier.service.RegistreJourService;
import com.ossanasur.cbconnect.utils.DataResponse;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/registres")
@RequiredArgsConstructor
@Tag(name = "Registres journaliers", description = "Registre ARRIVEE / DEPART tenu par la secrétaire")
@SecurityRequirement(name = "bearerAuth")
public class RegistreJourController {

    private final RegistreJourService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('SE','CSS','SECRETAIRE')")
    @Operation(summary = "Ouvrir un registre journalier (ARRIVEE ou DEPART)")
    public ResponseEntity<DataResponse<RegistreJourResponse>> ouvrir(
            @Valid @RequestBody RegistreJourRequest req,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(service.ouvrir(req, user.getUsername()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'un registre")
    public ResponseEntity<DataResponse<RegistreJourResponse>> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getByTrackingId(id));
    }

    @GetMapping("/du-jour")
    @Operation(summary = "Registre du jour courant ou d'une date donnée")
    public ResponseEntity<DataResponse<RegistreJourResponse>> getDuJour(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam TypeRegistre type) {
        return ResponseEntity.ok(service.getDuJour(date, type));
    }

    @GetMapping
    @Operation(summary = "Lister tous les registres")
    public ResponseEntity<DataResponse<List<RegistreJourResponse>>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}/courriers")
    @Operation(summary = "Courriers enregistrés dans ce registre")
    public ResponseEntity<DataResponse<List<CourrierResponse>>> getCourriers(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getCourriers(id));
    }

    @PatchMapping("/{id}/cloturer")
    @PreAuthorize("hasAnyRole('SE','CSS','SECRETAIRE')")
    @Operation(summary = "Clôturer le registre en fin de journée")
    public ResponseEntity<DataResponse<RegistreJourResponse>> cloturer(
            @PathVariable UUID id, @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(service.cloturer(id, user.getUsername()));
    }

    @PatchMapping("/{id}/viser")
    @PreAuthorize("hasAnyRole('SE','CSS')")
    @Operation(summary = "Viser (chef) le registre clôturé")
    public ResponseEntity<DataResponse<RegistreJourResponse>> viser(
            @PathVariable UUID id,
            @RequestBody VisaRegistreRequest req,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(service.viser(id, req, user.getUsername()));
    }
}
