package com.ossanasur.cbconnect.module.messagerie.dto.response;

import com.ossanasur.cbconnect.common.enums.TypeTemplateMailEnum;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MessageComplet(
        UUID courrierTrackingId,
        String messageIdImap,
        String de,
        List<String> a,
        List<String> cc,
        String sujet,
        String corpsHtml,
        String corpsTexte,
        LocalDateTime date,
        boolean lu,
        boolean traite,
        String sinistreNumeroLocal,
        UUID sinistreTrackingId,
        TypeTemplateMailEnum typeTemplate) {
}