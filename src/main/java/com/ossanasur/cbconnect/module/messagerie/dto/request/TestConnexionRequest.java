package com.ossanasur.cbconnect.module.messagerie.dto.request;

import com.ossanasur.cbconnect.common.enums.SecuriteMailProtocole;

// ════════════════════════════════════════════════════════════
//  Requête de test de connexion
// ════════════════════════════════════════════════════════════
public record TestConnexionRequest(
                String smtpHost,
                int smtpPort,
                SecuriteMailProtocole smtpSecurite,
                String username,
                String motDePasse) {
}