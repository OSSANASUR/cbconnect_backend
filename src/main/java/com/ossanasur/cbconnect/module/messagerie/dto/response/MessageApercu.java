package com.ossanasur.cbconnect.module.messagerie.dto.response;

import com.ossanasur.cbconnect.common.enums.StatutMailEnvoye;
import com.ossanasur.cbconnect.common.enums.TypeTemplateMailEnum;

import java.time.LocalDateTime;
import java.util.UUID;

// ════════════════════════════════════════════════════════════
//  Aperçu d'un message en liste (inbox / sent)
// ════════════════════════════════════════════════════════════
public record MessageApercu(
        UUID courrierTrackingId, // null si mail IMAP non encore en base
        String messageIdImap, // UID IMAP
        String de,
        String a,
        String sujet,
        String apercu, // 120 premiers chars du corps, texte seul
        LocalDateTime date,
        boolean lu,
        boolean traite,
        String sinistreNumeroLocal,
        UUID sinistreTrackingId,
        StatutMailEnvoye statutEnvoi,
        TypeTemplateMailEnum typeTemplate) {
}