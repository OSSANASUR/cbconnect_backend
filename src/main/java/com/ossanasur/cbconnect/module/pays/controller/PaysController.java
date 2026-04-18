package com.ossanasur.cbconnect.module.pays.controller;

import com.ossanasur.cbconnect.module.pays.dto.request.PaysRequest;
import com.ossanasur.cbconnect.module.pays.dto.response.PaysResponse;
import com.ossanasur.cbconnect.module.pays.service.PaysService;
import com.ossanasur.cbconnect.utils.DataResponse;
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
@RequestMapping("/v1/pays")
@RequiredArgsConstructor
@Tag(name = "Pays CEDEAO", description = "14 Etats membres")
@SecurityRequirement(name = "bearerAuth")
public class PaysController {
    private final PaysService paysService;

    @GetMapping
    public ResponseEntity<DataResponse<List<PaysResponse>>> getAll() {
        return ResponseEntity.ok(paysService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DataResponse<PaysResponse>> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(paysService.getByTrackingId(id));
    }

    @GetMapping("/iso/{code}")
    public ResponseEntity<DataResponse<PaysResponse>> getByIso(@PathVariable String code) {
        return ResponseEntity.ok(paysService.getByCodeIso(code));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DataResponse<PaysResponse>> create(@Valid @RequestBody PaysRequest r,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(paysService.create(r, u.getUsername()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DataResponse<PaysResponse>> update(@PathVariable UUID id, @Valid @RequestBody PaysRequest r,
            @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(paysService.update(id, r, u.getUsername()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DataResponse<Void>> delete(@PathVariable UUID id, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(paysService.delete(id, u.getUsername()));
    }
}
