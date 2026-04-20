package com.ossanasur.cbconnect.module.messagerie.dto.response;

// ════════════════════════════════════════════════════════════
//  Résultat du test de connexion
// ════════════════════════════════════════════════════════════
public record TestConnexionResponse(
                boolean smtpOk,
                boolean imapOk,
                String smtpMessage,
                String imapMessage,
                boolean success // true si les deux passent
) {
}
