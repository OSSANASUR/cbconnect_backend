package com.ossanasur.cbconnect.module.finance.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ossanasur.cbconnect.common.enums.TypeMotif;
import com.ossanasur.cbconnect.module.finance.dto.request.ParamMotifRequest;
import com.ossanasur.cbconnect.module.finance.dto.response.ParamMotifResponse;
import com.ossanasur.cbconnect.module.finance.service.impl.ParamMotifServiceImpl;
import com.ossanasur.cbconnect.utils.DataResponse;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/param-motifs")
@RequiredArgsConstructor
public class ParamMotifsController {

    private final ParamMotifServiceImpl service;

    @GetMapping
    public ResponseEntity<?> lister(
            @RequestParam(required = false) TypeMotif type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        if (type != null) {
            return ResponseEntity.ok(service.listerParType(type));
        }
        return ResponseEntity.ok(service.listerTous(page, size));
    }

    @GetMapping("/{trackingId}")
    public ResponseEntity<ParamMotifResponse> getOne(@PathVariable UUID trackingId) {
        return ResponseEntity.ok(service.getByTrackingId(trackingId));
    }

    @PostMapping
    public ResponseEntity<DataResponse<ParamMotifResponse>> creer(@Valid @RequestBody ParamMotifRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(201).body(service.creer(request, user.getUsername()));
    }

    @PutMapping("/{trackingId}")
    public ResponseEntity<DataResponse<ParamMotifResponse>> modifier(@PathVariable UUID trackingId,
            @Valid @RequestBody ParamMotifRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(service.modifier(trackingId, request, user.getUsername()));
    }

    @DeleteMapping("/{trackingId}")
    public ResponseEntity<DataResponse<Void>> supprimer(@PathVariable UUID trackingId,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(service.supprimer(trackingId, user.getUsername()));
    }
}