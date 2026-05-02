package com.ossanasur.cbconnect.module.messagerie.controller;

import com.ossanasur.cbconnect.module.messagerie.dto.request.EnvoyerMailRequest;
import com.ossanasur.cbconnect.module.messagerie.dto.request.PreviewTemplateRequest;
import com.ossanasur.cbconnect.module.messagerie.dto.response.MessageApercu;
import com.ossanasur.cbconnect.module.messagerie.dto.response.MessageComplet;
import com.ossanasur.cbconnect.module.messagerie.dto.response.PieceJointe;
import com.ossanasur.cbconnect.module.messagerie.dto.response.PreviewTemplateResponse;
import com.ossanasur.cbconnect.module.messagerie.dto.response.TemplateMailResponse;
import com.ossanasur.cbconnect.module.messagerie.service.MessagerieClientService;
import com.ossanasur.cbconnect.utils.DataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.Base64;

@RestController
@RequestMapping("/v1/messagerie")
@RequiredArgsConstructor
@Tag(name = "Client Messagerie", description = "Lecture/envoi de mails depuis CBConnect")
public class MessagerieClientController {

    private final MessagerieClientService clientService;

    @GetMapping("/inbox")
    @Operation(summary = "Boîte de réception (IMAP) — paginée")
    public ResponseEntity<DataResponse<MessagerieClientService.InboxPageResponse>> inbox(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String recherche,
            Authentication auth) {
        return ResponseEntity.ok(clientService.getInbox(auth.getName(), page, recherche));
    }

    @GetMapping("/inbox/non-lus")
    @Operation(summary = "Nombre de messages non lus")
    public ResponseEntity<DataResponse<Integer>> nonLus(Authentication auth) {
        return ResponseEntity.ok(clientService.getNonLus(auth.getName()));
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

    @PostMapping(value = "/envoyer", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Envoyer un mail (avec pièces jointes optionnelles)")
    public ResponseEntity<DataResponse<UUID>> envoyerMultipart(
            @RequestPart("data") EnvoyerMailRequest req,
            @RequestPart(value = "fichiers", required = false) List<MultipartFile> fichiers,
            Authentication auth) {
        return ResponseEntity.ok(clientService.envoyer(req, fichiers, auth.getName()));
    }

    @PostMapping(value = "/envoyer", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Envoyer un mail sans pièce jointe")
    public ResponseEntity<DataResponse<UUID>> envoyerJson(
            @RequestBody EnvoyerMailRequest req,
            Authentication auth) {
        return ResponseEntity.ok(clientService.envoyer(req, null, auth.getName()));
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

    /**
     * Télécharge une pièce jointe d'un mail IMAP.
     * GET /v1/messagerie/messages/{messageIdImap}/pj/{index}
     *
     * Retourne le fichier en téléchargement direct (Content-Disposition:
     * attachment).
     */
    @GetMapping("/messages/{messageIdImap}/pj/{index}")
    @Operation(summary = "Télécharger une pièce jointe")
    public ResponseEntity<byte[]> telechargerPj(
            @PathVariable String messageIdImap,
            @PathVariable int index,
            Authentication auth) {

        PieceJointe pj = clientService
                .telechargerPieceJointe(messageIdImap, index, auth.getName()).getData();

        byte[] bytes = Base64.getDecoder().decode(pj.contenuBase64());

        // Déduire le type MIME
        String mime = pj.contentType() != null
                ? pj.contentType().split(";")[0].trim()
                : "application/octet-stream";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + pj.nom() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, mime)
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(bytes.length))
                .body(bytes);
    }

    /**
     * Lit un message IMAP complet par son UID.
     * Appelé depuis le frontend quand courrierTrackingId est null.
     *
     * GET /v1/messagerie/imap/{uid}
     */
    @GetMapping("/imap/{uid}")
    @Operation(summary = "Lire un message IMAP par UID")
    public ResponseEntity<DataResponse<MessageComplet>> getMessageImap(
            @PathVariable String uid,
            Authentication auth) {
        return ResponseEntity.ok(clientService.getMessageImap(uid, auth.getName()));
    }

}