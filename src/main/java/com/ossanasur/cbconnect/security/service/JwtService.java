package com.ossanasur.cbconnect.security.service;

import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.utils.DataResponse;

import org.springframework.security.core.userdetails.UserDetails;
import java.util.Map;

public interface JwtService {
    Map<String, Object> generateTokens(Utilisateur user, boolean isMobile);

    DataResponse<Map<String, String>> refreshToken(String refreshToken);

    String extractUserEmail(String token);

    // [FIX #2] Retourne le claim "type" ("access" ou "refresh"). Utilise par
    // JwtAuthFilter
    // pour interdire qu'un refresh_token serve a acceder aux ressources protegees.
    String extractTokenType(String token);

    boolean isTokenValid(String token, UserDetails userDetails);

    boolean isTokenExpired(String token);

    boolean isTokenRevoked(String token);

    Utilisateur revokeToken(String token);

    long tokenExpireIn();

    long refreshTokenExpireIn();
}
