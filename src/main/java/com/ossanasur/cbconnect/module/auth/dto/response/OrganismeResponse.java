package com.ossanasur.cbconnect.module.auth.dto.response;
import com.ossanasur.cbconnect.common.enums.TypeOrganisme;
import java.time.LocalDate; import java.time.LocalDateTime; import java.util.List; import java.util.UUID;
public record OrganismeResponse(
    UUID organismeTrackingId, TypeOrganisme typeOrganisme, String raisonSociale,
    String code, String email, String responsable, List<String> contacts,
    String codePays, String codePaysBCB, Integer paysId,
    LocalDate dateCreation, String numeroAgrement, String apiEndpointUrl,
    boolean active, LocalDateTime createdAt
) {}
