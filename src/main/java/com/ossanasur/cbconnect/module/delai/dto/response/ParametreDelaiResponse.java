package com.ossanasur.cbconnect.module.delai.dto.response;

import com.ossanasur.cbconnect.common.enums.*;
import java.math.BigDecimal;

public record ParametreDelaiResponse(
        Integer id,
        String codeDelai,
        String libelle,
        TypeDelai typeDelai,
        CategorieActiviteDelai categorie,
        TypeSinistre typeSinistre,
        BigDecimal valeur,
        UniteDelai unite,
        String referenceJuridique,
        BigDecimal tauxPenalitePct,
        BigDecimal seuilAlerte1Pct,
        BigDecimal seuilAlerte2Pct,
        boolean modifiable,
        boolean actif
) {}
