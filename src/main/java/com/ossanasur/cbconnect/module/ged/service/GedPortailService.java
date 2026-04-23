package com.ossanasur.cbconnect.module.ged.service;

/**
 * Service de SSO vers OssanGED — émission et validation des tickets.
 *
 * Flux :
 *   1. L'utilisateur connecté à CBConnect clique "Ouvrir OssanGED"
 *   2. CBConnect génère un TICKET court (JWT signé, ~30 s de TTL)
 *      encapsulant l'username authentifié
 *   3. Le navigateur est redirigé vers http://ossanged/sso?ticket=<jwt>
 *   4. Nginx OssanGED fait un auth_request vers /v1/ged/portail/verify
 *      pour exchange le ticket contre un cookie de session long (~8 h)
 *   5. Les requêtes suivantes sont authentifiées via ce cookie ;
 *      nginx pose Remote-User sur chaque requête vers Paperless,
 *      qui auto-identifie (et auto-crée au besoin) l'utilisateur.
 */
public interface GedPortailService {

    /** Génère un ticket à usage unique encapsulant l'username. */
    String genererTicket(String username);

    /** Génère un cookie de session long (validité configurable) pour l'username. */
    String genererSession(String username);

    /** Valide un ticket court d'ouverture SSO et retourne l'username. */
    String validerTicket(String token);

    /** Valide un token de session long et retourne l'username. */
    String validerSession(String token);

    /**
     * Valide un token SSO et retourne l'username encodé.
     * Retourne null si invalide / expiré.
     */
    String validerToken(String token);

    /** Durée de vie (secondes) du cookie de session SSO. */
    long sessionTtlSeconds();
}
