package com.ossanasur.cbconnect.module.pv.controller;
import com.ossanasur.cbconnect.module.pv.dto.request.PvSinistreRequest;
import com.ossanasur.cbconnect.module.pv.dto.response.PvSinistreResponse;
import com.ossanasur.cbconnect.module.pv.service.PvSinistreService;
import com.ossanasur.cbconnect.utils.DataResponse;
import com.ossanasur.cbconnect.utils.PaginatedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List; import java.util.UUID;

@RestController
@RequestMapping("/v1/pv")
@RequiredArgsConstructor
@Tag(name = "PV Sinistres", description = "Enregistrement des proces-verbaux de police et gendarmerie")
@SecurityRequirement(name = "bearerAuth")
public class PvSinistreController {
    private final PvSinistreService pvService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR','SECRETAIRE')")
    @Operation(summary = "Enregistrer un PV")
    public ResponseEntity<DataResponse<PvSinistreResponse>> enregistrer(@Valid @RequestBody PvSinistreRequest r,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(pvService.enregistrer(r, u.getUsername()));
    }

    @GetMapping
    @Operation(summary = "Lister les PV (paginés, filtre par estComplet optionnel)")
    public ResponseEntity<PaginatedResponse<PvSinistreResponse>> lister(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean estComplet) {
        return ResponseEntity.ok(pvService.lister(page, size, estComplet));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DataResponse<PvSinistreResponse>> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(pvService.getByTrackingId(id));
    }

    @GetMapping("/sinistre/{sinistreId}")
    public ResponseEntity<DataResponse<List<PvSinistreResponse>>> getBySinistre(@PathVariable UUID sinistreId) {
        return ResponseEntity.ok(pvService.getBySinistre(sinistreId));
    }

    @GetMapping("/non-associes")
    @PreAuthorize("hasAnyRole('SE','CSS','SECRETAIRE')")
    @Operation(summary = "Lister les PV non associes a un sinistre")
    public ResponseEntity<DataResponse<List<PvSinistreResponse>>> getNonAssocies() {
        return ResponseEntity.ok(pvService.getNonAssocies());
    }

    @PatchMapping("/{pvId}/associer")
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR','SECRETAIRE')")
    @Operation(summary = "Associer un PV a un sinistre")
    public ResponseEntity<DataResponse<Void>> associer(@PathVariable UUID pvId, @RequestParam UUID sinistreId,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(pvService.associerSinistre(pvId, sinistreId, u.getUsername()));
    }

    @PostMapping(value = "/{pvTrackingId}/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR','SECRETAIRE')")
    @Operation(summary = "Téléverser le PV scanné (stocké dans le dossier GED du sinistre si associé, sinon en local)")
    public ResponseEntity<DataResponse<PvSinistreResponse>> uploadDocument(
            @PathVariable UUID pvTrackingId,
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "titre", required = false) String titre,
            @RequestPart(value = "typeDocument", required = false) String typeDocument,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(pvService.attacherDocument(pvTrackingId, file, titre, typeDocument, u.getUsername()));
    }

    @GetMapping("/{pvTrackingId}/document")
    @Operation(summary = "Télécharger ou prévisualiser le document du PV (GED ou fallback local)")
    public ResponseEntity<Resource> telechargerDocument(@PathVariable UUID pvTrackingId) {
        return pvService.telechargerDocument(pvTrackingId);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SE','CSS','SECRETAIRE')")
    public ResponseEntity<DataResponse<PvSinistreResponse>> modifier(@PathVariable UUID id,
            @Valid @RequestBody PvSinistreRequest r, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(pvService.modifier(id, r, u.getUsername()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SE','CSS')")
    public ResponseEntity<DataResponse<Void>> supprimer(@PathVariable UUID id,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(pvService.supprimer(id, u.getUsername()));
    }
}
