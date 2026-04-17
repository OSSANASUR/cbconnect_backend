package com.ossanasur.cbconnect.module.expertise.controller;
import com.ossanasur.cbconnect.module.expertise.dto.request.ExpertiseMedicaleRequest;
import com.ossanasur.cbconnect.module.expertise.dto.response.ExpertiseMedicaleResponse;
import com.ossanasur.cbconnect.module.expertise.service.ExpertiseMedicaleService;
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
import java.util.List; import java.util.UUID;
@RestController @RequestMapping("/v1/expertises-medicales") @RequiredArgsConstructor
@Tag(name="Expertises Medicales",description="Rapports d'expertise medicale des victimes")
@SecurityRequirement(name="bearerAuth")
public class ExpertiseMedicaleController {
    private final ExpertiseMedicaleService service;
    @PostMapping @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR')")
    public ResponseEntity<DataResponse<ExpertiseMedicaleResponse>> create(@Valid @RequestBody ExpertiseMedicaleRequest r, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(service.create(r, u.getUsername())); }
    @GetMapping("/{id}")
    public ResponseEntity<DataResponse<ExpertiseMedicaleResponse>> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getByTrackingId(id)); }
    @GetMapping("/victime/{victimeId}")
    public ResponseEntity<DataResponse<List<ExpertiseMedicaleResponse>>> getByVictime(@PathVariable UUID victimeId) {
        return ResponseEntity.ok(service.getByVictime(victimeId)); }
    @GetMapping("/en-attente") @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR')")
    @Operation(summary="Expertises sans rapport recu - alerte 20 jours")
    public ResponseEntity<DataResponse<List<ExpertiseMedicaleResponse>>> getEnAttente() {
        return ResponseEntity.ok(service.getEnAttente()); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('SE','CSS','REDACTEUR')")
    public ResponseEntity<DataResponse<ExpertiseMedicaleResponse>> update(@PathVariable UUID id, @Valid @RequestBody ExpertiseMedicaleRequest r, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(service.update(id, r, u.getUsername())); }
    @DeleteMapping("/{id}") @PreAuthorize("hasAnyRole('SE','CSS')")
    public ResponseEntity<DataResponse<Void>> delete(@PathVariable UUID id, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(service.delete(id, u.getUsername())); }
}
