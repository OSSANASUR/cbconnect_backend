package com.ossanasur.cbconnect.module.ged.controller;

import com.ossanasur.cbconnect.module.ged.service.GedPortailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Endpoints du portail SSO OssanGED.
 *
 * Flux :
 *  1. Frontend CBConnect → GET /v1/ged/portail/redirect (JWT bearer CBConnect)
 *     → 302 vers {ossanged-url}/sso?ticket=<jwt>
 *  2. Nginx OssanGED /sso fait POST /v1/ged/portail/exchange avec le ticket,
 *     reçoit un JWT session (~8 h), pose un cookie ossanged_sso et redirige vers /
 *  3. Chaque requête OssanGED passe par auth_request → /v1/ged/portail/verify
 *     (avec cookie), la réponse porte le header X-Auth-User repris par nginx
 *     et propagé à Paperless en Remote-User.
 */
@Slf4j
@RestController
@RequestMapping("/v1/ged/portail")
@RequiredArgsConstructor
@Tag(name = "OssanGED SSO", description = "SSO entre CBConnect et OssanGED (ticket + cookie de session)")
public class GedPortailController {

    private final GedPortailService portailService;

    @Value("${ossanged.public-url:http://localhost}")
    private String ossanGedPublicUrl;

    // ── 1a) URL d'ouverture SSO (JSON, pour axios/fetch) ────────────────────
    @GetMapping("/ticket")
    @Operation(summary = "Retourne une URL SSO à usage unique vers OssanGED")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<java.util.Map<String, String>> ticket(@AuthenticationPrincipal UserDetails user) {
        String ticket = portailService.genererTicket(user.getUsername());
        String url = ossanGedPublicUrl.replaceAll("/$", "")
                + "/sso?ticket=" + URLEncoder.encode(ticket, StandardCharsets.UTF_8);
        return ResponseEntity.ok(java.util.Map.of("url", url));
    }

    // ── 1b) Redirect direct (pour navigation serveur) ───────────────────────
    @GetMapping("/redirect")
    @Operation(summary = "Génère un ticket SSO et redirige vers OssanGED (navigation serveur)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> redirect(@AuthenticationPrincipal UserDetails user) {
        String ticket = portailService.genererTicket(user.getUsername());
        String url = ossanGedPublicUrl.replaceAll("/$", "")
                + "/sso?ticket=" + URLEncoder.encode(ticket, StandardCharsets.UTF_8);
        return ResponseEntity.status(302).header("Location", url).build();
    }

    // ── 2) Échange du ticket contre un JWT de session (appelé par nginx) ────
    @PostMapping("/exchange")
    @Operation(summary = "Échange un ticket SSO contre un JWT de session long")
    public ResponseEntity<String> exchange(@RequestParam("ticket") String ticket) {
        String username = portailService.validerToken(ticket);
        if (username == null) return ResponseEntity.status(401).build();
        String session = portailService.genererSession(username);
        // Header + body : nginx peut lire l'un ou l'autre
        return ResponseEntity.ok()
                .header("X-Auth-User", username)
                .header("X-Auth-Session", session)
                .header("X-Auth-Session-Max-Age", String.valueOf(portailService.sessionTtlSeconds()))
                .body(session);
    }

    // ── 3) Vérification à chaque requête (auth_request nginx) ───────────────
    @RequestMapping(value = "/verify", method = {RequestMethod.GET, RequestMethod.HEAD})
    @Operation(summary = "Vérifie un cookie/session SSO et retourne l'username")
    public ResponseEntity<Void> verify(HttpServletRequest request,
                                       @RequestHeader(value = "X-Original-URI", required = false) String uri) {
        // Cherche le cookie de session
        String token = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("ossanged_sso".equals(c.getName())) { token = c.getValue(); break; }
            }
        }
        if (token == null) {
            // Fallback : header Authorization si utilisé par un outil tiers
            String auth = request.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) token = auth.substring(7);
        }
        String username = portailService.validerToken(token);
        if (username == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok().header("X-Auth-User", username).build();
    }

    // ── 4) Logout — invalide le cookie côté nginx (idempotent) ─────────────
    @GetMapping("/logout")
    @Operation(summary = "Redirige vers OssanGED pour déconnecter la session SSO")
    public void logout(HttpServletResponse response) throws IOException {
        String url = ossanGedPublicUrl.replaceAll("/$", "") + "/sso/logout";
        response.sendRedirect(url);
    }
}
