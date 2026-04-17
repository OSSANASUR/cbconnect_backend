package com.ossanasur.cbconnect.module.expertise.controller;
import com.ossanasur.cbconnect.common.enums.TypeExpert;
import com.ossanasur.cbconnect.module.expertise.dto.request.ExpertRequest;
import com.ossanasur.cbconnect.module.expertise.dto.response.ExpertResponse;
import com.ossanasur.cbconnect.module.expertise.service.ExpertService;
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
import java.util.List; import java.util.UUID;
@RestController @RequestMapping("/v1/experts") @RequiredArgsConstructor
@Tag(name="Experts",description="Experts medicaux et automobiles agrees")
@SecurityRequirement(name="bearerAuth")
public class ExpertController {
    private final ExpertService expertService;
    @PostMapping @PreAuthorize("hasAnyRole('SE','CSS','ADMIN')")
    public ResponseEntity<DataResponse<ExpertResponse>> create(@Valid @RequestBody ExpertRequest r, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(expertService.create(r, u.getUsername())); }
    @GetMapping("/{id}") public ResponseEntity<DataResponse<ExpertResponse>> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(expertService.getByTrackingId(id)); }
    @GetMapping("/type/{type}") public ResponseEntity<DataResponse<List<ExpertResponse>>> getByType(@PathVariable TypeExpert type) {
        return ResponseEntity.ok(expertService.getAllActifsByType(type)); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('SE','ADMIN')")
    public ResponseEntity<DataResponse<ExpertResponse>> update(@PathVariable UUID id, @Valid @RequestBody ExpertRequest r, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(expertService.update(id, r, u.getUsername())); }
    @DeleteMapping("/{id}") @PreAuthorize("hasAnyRole('SE','ADMIN')")
    public ResponseEntity<DataResponse<Void>> delete(@PathVariable UUID id, @AuthenticationPrincipal UserDetails u) {
        return ResponseEntity.ok(expertService.delete(id, u.getUsername())); }
}
