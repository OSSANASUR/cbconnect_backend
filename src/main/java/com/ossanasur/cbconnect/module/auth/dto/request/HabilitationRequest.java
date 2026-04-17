package com.ossanasur.cbconnect.module.auth.dto.request;
import com.ossanasur.cbconnect.common.enums.ActionHabilitation;
import com.ossanasur.cbconnect.common.enums.TypeAccesHabilitation;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
public record HabilitationRequest(
    @NotBlank String codeHabilitation,
    @NotBlank String libelleHabilitation,
    String description,
    ActionHabilitation action,
    TypeAccesHabilitation typeAcces,
    UUID moduleTrackingId
) {}
