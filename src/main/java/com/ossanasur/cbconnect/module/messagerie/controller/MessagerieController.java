package com.ossanasur.cbconnect.module.messagerie.controller;

import com.ossanasur.cbconnect.module.messagerie.dto.request.ChangeMotDePasseMailRequest;
import com.ossanasur.cbconnect.module.messagerie.dto.request.ConfigMailRequest;
import com.ossanasur.cbconnect.module.messagerie.dto.request.SignatureRequest;
import com.ossanasur.cbconnect.module.messagerie.dto.request.TestConnexionRequest;
import com.ossanasur.cbconnect.module.messagerie.dto.response.ConfigMailResponse;
import com.ossanasur.cbconnect.module.messagerie.dto.response.DetectionDomainResponse;
import com.ossanasur.cbconnect.module.messagerie.dto.response.TestConnexionResponse;
import com.ossanasur.cbconnect.module.messagerie.service.MessagerieService;
import com.ossanasur.cbconnect.utils.DataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/messagerie")
@RequiredArgsConstructor
@Tag(name = "Messagerie", description = "Configuration SMTP/IMAP personnelle et signature")
public class MessagerieController {

    private final MessagerieService messagerieService;

    /**
     * Détecte automatiquement les paramètres SMTP/IMAP à partir de l'email de
     * l'utilisateur.
     * GET /v1/messagerie/detecter?email=user@gmail.com
     */
    @GetMapping("/detecter")
    @Operation(summary = "Détection automatique SMTP/IMAP par domaine email")
    public ResponseEntity<DataResponse<DetectionDomainResponse>> detecter(
            @RequestParam String email) {
        return ResponseEntity.ok(messagerieService.detecterDomaine(email));
    }

    /**
     * Test de connexion SMTP avec les paramètres fournis.
     * POST /v1/messagerie/tester
     */
    @PostMapping("/tester")
    @Operation(summary = "Tester la connexion SMTP")
    public ResponseEntity<DataResponse<TestConnexionResponse>> tester(
            @RequestBody TestConnexionRequest req) {
        return ResponseEntity.ok(messagerieService.testerConnexion(req));
    }

    /**
     * Récupère la configuration mail de l'utilisateur connecté (sans mot de passe).
     * GET /v1/messagerie/config
     */
    @GetMapping("/config")
    @Operation(summary = "Récupérer la configuration mail de l'utilisateur connecté")
    public ResponseEntity<DataResponse<ConfigMailResponse>> getConfig(Authentication auth) {
        return ResponseEntity.ok(messagerieService.getConfig(auth.getName()));
    }

    /**
     * Sauvegarde la configuration complète (wizard).
     * POST /v1/messagerie/config
     */
    @PostMapping("/config")
    @Operation(summary = "Sauvegarder la configuration mail")
    public ResponseEntity<DataResponse<ConfigMailResponse>> sauvegarder(
            @Valid @RequestBody ConfigMailRequest req,
            Authentication auth) {
        return ResponseEntity.ok(messagerieService.sauvegarderConfig(req, auth.getName()));
    }

    /**
     * Met à jour uniquement la signature HTML.
     * PATCH /v1/messagerie/signature
     */
    @PatchMapping("/signature")
    @Operation(summary = "Mettre à jour la signature mail")
    public ResponseEntity<DataResponse<ConfigMailResponse>> signature(
            @RequestBody SignatureRequest req,
            Authentication auth) {
        return ResponseEntity.ok(messagerieService.mettreAJourSignature(req, auth.getName()));
    }

    /**
     * Change uniquement le mot de passe mail.
     * PATCH /v1/messagerie/mot-de-passe
     */
    @PatchMapping("/mot-de-passe")
    @Operation(summary = "Changer le mot de passe de la boîte mail")
    public ResponseEntity<DataResponse<Void>> motDePasse(
            @Valid @RequestBody ChangeMotDePasseMailRequest req,
            Authentication auth) {
        return ResponseEntity.ok(messagerieService.changerMotDePasse(req, auth.getName()));
    }
}
