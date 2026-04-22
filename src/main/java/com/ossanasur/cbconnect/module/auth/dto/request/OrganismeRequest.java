package com.ossanasur.cbconnect.module.auth.dto.request;
import com.ossanasur.cbconnect.common.enums.TypeOrganisme;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
public record OrganismeRequest(
    @NotNull TypeOrganisme typeOrganisme,
    @NotBlank String raisonSociale,
    @NotBlank String code,
    @Email @NotBlank String email,
    String responsable,
    List<String> contacts,
    String codePays,
    String codePaysBCB,
    Integer paysId,
    LocalDate dateCreation,
    String numeroAgrement,
    String apiEndpointUrl,
    boolean active,
    // Coordonnees (V30)
    String adresse,
    String boitePostale,
    String ville,
    String telephonePrincipal,
    String fax,
    String siteWeb
) {}
