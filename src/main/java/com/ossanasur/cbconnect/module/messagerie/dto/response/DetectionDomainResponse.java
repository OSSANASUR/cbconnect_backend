package com.ossanasur.cbconnect.module.messagerie.dto.response;

import com.ossanasur.cbconnect.common.enums.SecuriteMailProtocole;

// ════════════════════════════════════════════════════════════
//  Détection automatique par domaine (retournée AVANT config)
// ════════════════════════════════════════════════════════════
public record DetectionDomainResponse(

                String domaine, // ex: "gmail.com"
                String fournisseur, // ex: "Gmail / Google Workspace"
                String logoUrl, // ex: icône du fournisseur pour l'UI
                boolean detectionReussie, // false = domaine inconnu, config manuelle

                // SMTP pré-rempli
                String smtpHost,
                int smtpPort,
                SecuriteMailProtocole smtpSecurite,

                // IMAP pré-rempli
                String imapHost,
                int imapPort,
                SecuriteMailProtocole imapSecurite,

                // Consigne spécifique au fournisseur
                String consigne // ex: "Activez l'accès IMAP dans les paramètres Gmail"
) {
}
