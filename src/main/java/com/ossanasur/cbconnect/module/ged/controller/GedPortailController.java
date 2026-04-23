package com.ossanasur.cbconnect.module.ged.controller;

import com.ossanasur.cbconnect.module.ged.service.GedPortailService;
import com.ossanasur.cbconnect.module.ged.service.OssanGedClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;

/**
 * Endpoints du portail SSO OssanGED.
 *
 * Flux :
 *  1. Frontend CBConnect → GET /v1/ged/portail/ticket (JWT bearer CBConnect)
 *     → URL one-shot /v1/ged/portail/start?ticket=<jwt>
 *  2. Backend CBConnect /start valide le ticket, pose le cookie ossanged_sso
 *     puis redirige le navigateur vers {ossanged-url}/
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
    private final OssanGedClientService gedClient;

    @Value("${ossanged.public-url:http://localhost}")
    private String ossanGedPublicUrl;

    // ── 1a) URL d'ouverture SSO (JSON, pour axios/fetch) ────────────────────
    @GetMapping("/ticket")
    @Operation(summary = "Retourne une URL SSO à usage unique vers OssanGED")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<java.util.Map<String, String>> ticket(@AuthenticationPrincipal UserDetails user) {
        String ticket = portailService.genererTicket(user.getUsername());
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/v1/ged/portail/start")
                .queryParam("ticket", ticket)
                .build()
                .toUriString();
        return ResponseEntity.ok(java.util.Map.of("url", url));
    }

    // ── 1b) Redirect direct (pour navigation serveur) ───────────────────────
    @GetMapping("/redirect")
    @Operation(summary = "Génère un ticket SSO et redirige vers OssanGED (navigation serveur)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> redirect(@AuthenticationPrincipal UserDetails user) {
        String ticket = portailService.genererTicket(user.getUsername());
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/v1/ged/portail/start")
                .queryParam("ticket", ticket)
                .build()
                .toUriString();
        return ResponseEntity.status(302).header("Location", url).build();
    }

    // ── 2) Démarrage SSO navigateur : pose le cookie puis redirige ──────────
    @GetMapping("/start")
    @Operation(summary = "Valide un ticket SSO, pose le cookie de session et redirige vers OssanGED")
    public ResponseEntity<Void> start(@RequestParam("ticket") String ticket) {
        String username = portailService.validerTicket(ticket);
        if (username == null) {
            String deniedUrl = ossanGedPublicUrl.replaceAll("/$", "") + "/sso/denied";
            return ResponseEntity.status(302).header(HttpHeaders.LOCATION, deniedUrl).build();
        }

        // Synchrone : l'utilisateur doit être superuser AVANT d'arriver sur OssanGED
        gedClient.provisionnerUtilisateur(username);

        String session = portailService.genererSession(username);
        ResponseCookie cookie = ResponseCookie.from("ossanged_sso", session)
                .httpOnly(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(portailService.sessionTtlSeconds())
                .build();

        String targetUrl = ossanGedPublicUrl.replaceAll("/$", "") + "/";
        return ResponseEntity.status(302)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .header(HttpHeaders.LOCATION, targetUrl)
                .build();
    }

    // ── 3) Échange du ticket contre un JWT de session (appelé par nginx) ────
    @RequestMapping(value = "/exchange", method = {RequestMethod.GET, RequestMethod.POST})
    @Operation(summary = "Échange un ticket SSO contre un JWT de session long")
    public ResponseEntity<String> exchange(@RequestParam("ticket") String ticket) {
        String username = portailService.validerTicket(ticket);
        if (username == null) return ResponseEntity.status(401).build();
        gedClient.provisionnerUtilisateur(username);
        String session = portailService.genererSession(username);
        return ResponseEntity.ok()
                .header("X-Auth-User", username)
                .header("X-Auth-Session", session)
                .header("X-Auth-Session-Max-Age", String.valueOf(portailService.sessionTtlSeconds()))
                .body(session);
    }

    // ── 4) Vérification à chaque requête (auth_request nginx) ───────────────
    @RequestMapping(value = "/verify", method = {RequestMethod.GET, RequestMethod.HEAD})
    @Operation(summary = "Vérifie un cookie/session SSO et retourne l'username")
    public ResponseEntity<Void> verify(HttpServletRequest request,
                                       @RequestHeader(value = "X-Original-URI", required = false) String uri) {
        // 1) Cookie SSO CBConnect
        String token = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("ossanged_sso".equals(c.getName())) { token = c.getValue(); break; }
            }
        }
        if (token == null) {
            String auth = request.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) token = auth.substring(7);
        }
        String username = portailService.validerSession(token);
        if (username != null) {
            return ResponseEntity.ok().header("X-Auth-User", username).build();
        }

        // 2) Fallback : session Django native (login direct via /accounts/login/)
        String cookieHeader = request.getHeader("Cookie");
        String paperlessUser = gedClient.verifierSessionPaperless(cookieHeader);
        if (paperlessUser != null) {
            return ResponseEntity.ok().header("X-Auth-User", paperlessUser).build();
        }

        return ResponseEntity.status(401).build();
    }

    // ── 5) Bridge session Django → cookie SSO (connexion directe admin) ────
    @GetMapping("/bridge")
    @Operation(summary = "Convertit une session Django Paperless en cookie SSO CBConnect (admin direct)")
    public ResponseEntity<Void> bridge(HttpServletRequest request) {
        String cookieHeader = request.getHeader("Cookie");
        String username = gedClient.verifierSessionPaperless(cookieHeader);
        if (username == null) {
            String loginUrl = ossanGedPublicUrl.replaceAll("/$", "") + "/accounts/login/?next=/sso-bridge";
            return ResponseEntity.status(302).header(HttpHeaders.LOCATION, loginUrl).build();
        }
        gedClient.provisionnerUtilisateur(username);
        String session = portailService.genererSession(username);
        ResponseCookie cookie = ResponseCookie.from("ossanged_sso", session)
                .httpOnly(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(portailService.sessionTtlSeconds())
                .build();
        return ResponseEntity.status(302)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .header(HttpHeaders.LOCATION, ossanGedPublicUrl.replaceAll("/$", "") + "/")
                .build();
    }

    // ── 6) Logout — invalide le cookie côté nginx (idempotent) ─────────────
    @GetMapping("/logout")
    @Operation(summary = "Redirige vers OssanGED pour déconnecter la session SSO")
    public void logout(HttpServletResponse response) throws IOException {
        String url = ossanGedPublicUrl.replaceAll("/$", "") + "/sso/logout";
        response.sendRedirect(url);
    }
}
