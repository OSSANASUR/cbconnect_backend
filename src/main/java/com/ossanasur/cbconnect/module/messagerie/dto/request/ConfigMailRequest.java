package com.ossanasur.cbconnect.module.messagerie.dto.request;

import com.ossanasur.cbconnect.common.enums.SecuriteMailProtocole;
import jakarta.validation.constraints.NotBlank;

// ════════════════════════════════════════════════════════════
//  Requête de sauvegarde de la configuration
// ════════════════════════════════════════════════════════════
public record ConfigMailRequest(

                String nomAffiche,

                @NotBlank String smtpHost,
                int smtpPort,
                SecuriteMailProtocole smtpSecurite,
                String smtpUsername,

                /** Mot de passe en clair — sera chiffré côté serveur avant stockage */
                @NotBlank String motDePasse,

                @NotBlank String imapHost,
                int imapPort,
                SecuriteMailProtocole imapSecurite,

                String signature) {
}
