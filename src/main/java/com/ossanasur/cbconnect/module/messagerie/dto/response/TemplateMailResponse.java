package com.ossanasur.cbconnect.module.messagerie.dto.response;

import com.ossanasur.cbconnect.common.enums.TypeTemplateMailEnum;

import java.util.UUID;

public record TemplateMailResponse(
                UUID trackingId,
                TypeTemplateMailEnum typeTemplate,
                String nom,
                String description,
                String sujet, // brut avec {{variables}}
                String corpsHtml, // brut avec {{variables}}
                boolean actif) {
}
