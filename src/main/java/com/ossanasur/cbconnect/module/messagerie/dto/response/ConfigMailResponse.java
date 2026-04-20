package com.ossanasur.cbconnect.module.messagerie.dto.response;

import com.ossanasur.cbconnect.common.enums.SecuriteMailProtocole;

import java.time.LocalDateTime;
import java.util.UUID;

// ════════════════════════════════════════════════════════════
//  Réponse configuration (sans mot de passe)
// ════════════════════════════════════════════════════════════
public record ConfigMailResponse(

                UUID trackingId,
                String emailExpediteur,
                String nomAffiche,
                boolean estConfiguree,

                String smtpHost,
                int smtpPort,
                SecuriteMailProtocole smtpSecurite,
                String smtpUsername,
                boolean smtpAuth,

                String imapHost,
                int imapPort,
                SecuriteMailProtocole imapSecurite,

                String signature,
                LocalDateTime derniereSynchro,
                int nbMessagesNonLus) {
}
