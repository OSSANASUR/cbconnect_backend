package com.ossanasur.cbconnect.module.messagerie.controller;

import com.ossanasur.cbconnect.module.messagerie.dto.request.EnvoyerMailRequest;
import com.ossanasur.cbconnect.module.messagerie.dto.request.PreviewTemplateRequest;
import com.ossanasur.cbconnect.module.messagerie.dto.response.MessageApercu;
import com.ossanasur.cbconnect.module.messagerie.dto.response.MessageComplet;
import com.ossanasur.cbconnect.module.messagerie.dto.response.PreviewTemplateResponse;
import com.ossanasur.cbconnect.module.messagerie.dto.response.TemplateMailResponse;
import com.ossanasur.cbconnect.module.messagerie.service.MessagerieClientService;
import com.ossanasur.cbconnect.utils.DataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/messagerie")
@RequiredArgsConstructor
@Tag(name = "Client Messagerie", description = "Lecture/envoi de mails depuis CBConnect")
public class MessagerieClientController {

    private final MessagerieClientService clientService;

    @GetMapping("/inbox")
    @Operation(summary = "Boîte de réception (IMAP)")
    public ResponseEntity<DataResponse<List<MessageApercu>>> inbox(Authentication auth) {
        return ResponseEntity.ok(clientService.getInbox(auth.getName()));
    }

    @GetMapping("/sent")
    @Operation(summary = "Mails envoyés depuis CBConnect")
    public ResponseEntity<DataResponse<List<MessageApercu>>> sent(Authentication auth) {
        return ResponseEntity.ok(clientService.getSent(auth.getName()));
    }

    @GetMapping("/messages/{courrierTrackingId}")
    @Operation(summary = "Lire un message complet")
    public ResponseEntity<DataResponse<MessageComplet>> getMessage(
            @PathVariable UUID courrierTrackingId, Authentication auth) {
        return ResponseEntity.ok(clientService.getMessage(courrierTrackingId, auth.getName()));
    }

    @PostMapping("/envoyer")
    @Operation(summary = "Envoyer un mail (avec ou sans template)")
    public ResponseEntity<DataResponse<UUID>> envoyer(
            @Valid @RequestBody EnvoyerMailRequest req, Authentication auth) {
        return ResponseEntity.ok(clientService.envoyer(req, auth.getName()));
    }

    @GetMapping("/templates")
    @Operation(summary = "Liste des templates mail disponibles")
    public ResponseEntity<DataResponse<List<TemplateMailResponse>>> templates() {
        return ResponseEntity.ok(clientService.getTemplates());
    }

    @PostMapping("/templates/preview")
    @Operation(summary = "Prévisualisation d'un template avec substitution des variables")
    public ResponseEntity<DataResponse<PreviewTemplateResponse>> preview(
            @RequestBody PreviewTemplateRequest req) {
        return ResponseEntity.ok(clientService.previewTemplate(req));
    }
}
