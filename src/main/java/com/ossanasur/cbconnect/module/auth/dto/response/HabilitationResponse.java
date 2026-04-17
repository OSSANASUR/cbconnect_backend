package com.ossanasur.cbconnect.module.auth.dto.response;
import com.ossanasur.cbconnect.common.enums.ActionHabilitation;
import com.ossanasur.cbconnect.common.enums.TypeAccesHabilitation;
import java.util.UUID;
public record HabilitationResponse(
    UUID habilitationTrackingId, String codeHabilitation, String libelleHabilitation,
    String description, ActionHabilitation action, TypeAccesHabilitation typeAcces,
    String moduleNom, UUID moduleTrackingId
) {}
