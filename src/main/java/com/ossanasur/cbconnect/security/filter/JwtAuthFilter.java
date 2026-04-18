package com.ossanasur.cbconnect.security.filter;

import com.ossanasur.cbconnect.security.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response); return;
        }
        final String jwt = authHeader.substring(7);
        // [FIX #2] Seul un access_token peut donner acces aux ressources protegees.
        // Un refresh_token doit UNIQUEMENT servir a obtenir un nouvel access_token sur /refresh-token.
        // Avant ce fix, les deux etaient indistinguables pour le filtre (faille de securite).
        String tokenType;
        try {
            tokenType = jwtService.extractTokenType(jwt);
        } catch (Exception e) {
            // Token illisible / signature invalide / dechiffrement impossible
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); return;
        }
        if (!"access".equals(tokenType)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); return;
        }
        if (jwtService.isTokenRevoked(jwt)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); return;
        }
        final String email = jwtService.extractUserEmail(jwt);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (email != null && auth == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        chain.doFilter(request, response);
    }
}
