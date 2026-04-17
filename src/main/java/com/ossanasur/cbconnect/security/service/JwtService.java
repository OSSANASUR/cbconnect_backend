package com.ossanasur.cbconnect.security.service;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.utils.DataResponse;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Map;

public interface JwtService {
    Map<String, Object> generateTokens(Utilisateur user, boolean isMobile);
    DataResponse<Map<String, String>> refreshToken(String refreshToken);
    String extractUserEmail(String token);
    boolean isTokenValid(String token, UserDetails userDetails);
    boolean isTokenExpired(String token);
    boolean isTokenRevoked(String token);
    void revokeToken(String token);
    long tokenExpireIn();
    long refreshTokenExpireIn();
}
