package com.ossanasur.cbconnect.module.reprise.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record RepriseOrganismesRequest(
                @NotEmpty List<OrganismeRepriseDto> organismes) {
        public record OrganismeRepriseDto(
                        String raisonSociale,
                        String code,
                        String pays // null pour feuille 2 (Ghana, Nigeria, etc.)
        ) {
        }
}
