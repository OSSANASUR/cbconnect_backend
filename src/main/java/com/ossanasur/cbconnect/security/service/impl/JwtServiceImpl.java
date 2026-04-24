package com.ossanasur.cbconnect.security.service.impl;

import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.module.auth.repository.UtilisateurRepository;
import com.ossanasur.cbconnect.security.entity.Token;
import com.ossanasur.cbconnect.security.repository.TokenRepository;
import com.ossanasur.cbconnect.security.service.JwtService;
import com.ossanasur.cbconnect.utils.DataResponse;
import com.ossanasur.cbconnect.utils.EncryptionToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final UtilisateurRepository utilisateurRepository;
    private final TokenRepository tokenRepository;
    private final EncryptionToken encryptionToken;

    @Value("${jwt.jwtSecret}")
    private String key;
    @Value("${jwt.jwtExpiration}")
    private long accessDuration;
    @Value("${jwt.jwtRefreshExpiration}")
    private long refreshDuration;
    @Value("${jwt.jwtMobileExpiration}")
    private long mobileDuration;

    private String generateToken(String subject, Map<String, Object> claims, long durationMs) {
        return Jwts.builder()
                .claims(claims).subject(subject).issuer("@55@nAsnr-BNCB")
                .issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + durationMs))
                .signWith(generateKey()).compact();
    }

    @Override
    public Map<String, Object> generateTokens(Utilisateur user, boolean isMobile) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("trackingId", user.getUtilisateurTrackingId());
        claims.put("nom", user.getNom());
        claims.put("prenoms", user.getPrenoms());
        claims.put("profil", user.getProfil() != null ? user.getProfil().getProfilNom() : null);
        // [FIX #2] Marque explicite du type. Lu par JwtAuthFilter pour rejeter les
        // refresh tokens.
        claims.put("type", "access");
        claims.put("vagad", "miolnir");

        // [FIX #1] Subject = email (et non username) pour rester coherent avec
        // CustomUserDetailsService qui construit toujours UserDetails.username = email.
        // Avant ce fix, quand user.username != user.email, isTokenValid echouait sur
        // l'access_token.
        String accessToken = generateToken(user.getEmail(), claims, accessDuration);
        String encryptedAccess = encryptionToken.encrypt(accessToken);

        String encryptedRefresh = null;
        if (!isMobile) {
            Map<String, Object> refreshClaims = new HashMap<>();
            refreshClaims.put("trackingId", user.getUtilisateurTrackingId());
            // [FIX #2] Marque explicite du type. Ce token sera rejete par JwtAuthFilter.
            refreshClaims.put("type", "refresh");
            refreshClaims.put("vagad", "thor");
            String refresh = generateToken(user.getEmail(), refreshClaims, refreshDuration);
            encryptedRefresh = encryptionToken.encrypt(refresh);
        }

        LocalDateTime expireAt = LocalDateTime.ofInstant(
                Instant.now().plusMillis(isMobile ? mobileDuration : refreshDuration), ZoneId.systemDefault());

        Token token = Token.builder()
                .tokenTrackingId(UUID.randomUUID()).accessToken(encryptedAccess)
                .refreshToken(encryptedRefresh).mobileToken(isMobile).isValid(true)
                .expireAt(expireAt).user(user).activeData(true).build();
        tokenRepository.save(token);
        tokenRepository.invalidateOtherTokens(user.getUtilisateurTrackingId(), token.getHistoriqueId());

        Map<String, Object> result = new HashMap<>();
        result.put("access_token", encryptedAccess);
        result.put("access_expire_in", accessDuration);
        if (!isMobile) {
            result.put("refresh_token", encryptedRefresh);
            result.put("refresh_expire_in", refreshDuration);
        }
        return result;
    }

    @Override
    public DataResponse<Map<String, String>> refreshToken(String refreshToken) {
        // [FIX #3] On refuse qu'un access_token soit envoye sur /refresh-token.
        // Seul un token portant le claim type="refresh" doit pouvoir renouveler
        // l'acces.
        if (!"refresh".equals(extractTokenType(refreshToken))) {
            throw new RuntimeException("Le jeton fourni n'est pas un refresh token");
        }
        Token rToken = tokenRepository.findByAccessTokenOrRefreshToken(refreshToken, refreshToken)
                .orElseThrow(() -> new RessourceNotFoundException("Jeton inexistant"));
        String decrypted = encryptionToken.decrypt(rToken.getRefreshToken());
        if (isTokenExpired(decrypted))
            throw new RuntimeException("Refresh token expire");
        String email = extractUserEmail(decrypted);
        Utilisateur user = utilisateurRepository.findByEmailAndActiveDataTrueAndDeletedDataFalse(email)
                .orElseThrow(() -> new RessourceNotFoundException("Utilisateur introuvable"));
        Map<String, Object> claims = new HashMap<>();
        claims.put("trackingId", user.getUtilisateurTrackingId());
        // [FIX #2] On remet le claim type="access" sur le nouveau token, sinon
        // JwtAuthFilter le rejettera.
        claims.put("type", "access");
        claims.put("vagad", "miolnir");
        String newAccess = generateToken(email, claims, accessDuration);
        // On chiffre avant stockage ET avant retour au client : coherent avec
        // generateTokens()
        // et avec existsActiveToken() qui compare sur la forme chiffree en base.
        String encryptedNewAccess = encryptionToken.encrypt(newAccess);
        tokenRepository.updateAccessToken(rToken.getHistoriqueId(), encryptedNewAccess);
        return DataResponse.success("Jeton renouvele",
                Map.of("access_token", encryptedNewAccess, "refresh_token", refreshToken));
    }

    @Override
    public String extractUserEmail(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    // [FIX #2] Lit le claim "type" ajoute dans generateTokens() / refreshToken().
    // Renvoie null si le token n'a pas ce claim (ex: anciens tokens emis avant ce
    // fix).
    @Override
    public String extractTokenType(String token) {
        return extractClaims(token, c -> (String) c.get("type"));
    }

    @Override
    public boolean isTokenExpired(String token) {
        return extractClaims(token, Claims::getExpiration).before(new Date());
    }

    @Override
    public boolean isTokenRevoked(String token) {
        return !tokenRepository.existsActiveToken(token);
    }

    @Override
    public Utilisateur revokeToken(String token) {
        Token oToken = tokenRepository
                .findByAccessTokenOrRefreshToken(token, token)
                .orElseThrow(() -> new RuntimeException("Token introuvable"));

        oToken.setValid(false);
        oToken.setExpireAt(LocalDateTime.now());
        tokenRepository.save(oToken);

        return oToken.getUser();
    }

    @Override
    public long tokenExpireIn() {
        return accessDuration;
    }

    @Override
    public long refreshTokenExpireIn() {
        return refreshDuration;
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractUserEmail(token).equals(userDetails.getUsername()) && !isTokenExpired(token)
                && !isTokenRevoked(token);
    }

    private <T> T extractClaims(String token, Function<Claims, T> resolver) {
        return resolver.apply(parseClaims(token));
    }

    private Claims parseClaims(String token) {
        String t;
        try {
            Jwts.parser().verifyWith(generateKey()).build().parseSignedClaims(token);
            t = token;
        } catch (Exception e) {
            t = encryptionToken.decrypt(token);
        }
        return Jwts.parser().verifyWith(generateKey()).build().parseSignedClaims(t).getPayload();
    }

    private SecretKey generateKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(this.key));
    }
}
