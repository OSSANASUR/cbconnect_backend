package com.ossanasur.cbconnect.module.ged.service.impl;

import com.ossanasur.cbconnect.module.ged.service.GedPortailService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Service
public class GedPortailServiceImpl implements GedPortailService {

    /**
     * Secret dédié SSO — distinct du JWT d'authentification CBConnect pour
     * isoler les périmètres et permettre la rotation indépendante.
     * Fallback sur un dérivé du secret JWT principal si non défini.
     */
    @Value("${ossanged.sso.secret:}")
    private String ssoSecret;

    @Value("${jwt.jwtSecret}")
    private String fallbackJwtSecret;

    @Value("${ossanged.sso.ticket-ttl-seconds:30}")
    private long ticketTtlSeconds;

    @Value("${ossanged.sso.session-ttl-seconds:28800}") // 8 h par défaut
    private long sessionTtlSeconds;

    private SecretKey key;

    @PostConstruct
    void init() {
        String secret = (ssoSecret != null && !ssoSecret.isBlank()) ? ssoSecret : fallbackJwtSecret;
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, Math.min(bytes.length, 32));
            bytes = padded;
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    @Override
    public String genererTicket(String username) {
        return emit(username, "ticket", ticketTtlSeconds);
    }

    @Override
    public String genererSession(String username) {
        return emit(username, "session", sessionTtlSeconds);
    }

    @Override
    public String validerTicket(String token) {
        return validerToken(token, "ticket");
    }

    @Override
    public String validerSession(String token) {
        return validerToken(token, "session");
    }

    @Override
    public String validerToken(String token) {
        return validerToken(token, null);
    }

    private String validerToken(String token, String expectedType) {
        if (token == null || token.isBlank()) return null;
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String tokenType = claims.get("typ", String.class);
            if (expectedType != null && !expectedType.equals(tokenType)) {
                log.debug("Token SSO invalide : type {} inattendu", tokenType);
                return null;
            }
            String username = claims.getSubject();
            return (username != null && !username.isBlank()) ? username : null;
        } catch (Exception e) {
            log.debug("Token SSO invalide : {}", e.getMessage());
            return null;
        }
    }

    @Override
    public long sessionTtlSeconds() {
        return sessionTtlSeconds;
    }

    private String emit(String username, String type, long ttlSeconds) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(username)
                .claim("typ", type)
                .issuer("cbconnect")
                .audience().add("ossanged").and()
                .issuedAt(new Date(now))
                .expiration(new Date(now + ttlSeconds * 1000L))
                .signWith(key)
                .compact();
    }
}
