package com.ossanasur.cbconnect.module.reclamation.dto.request;

import com.ossanasur.cbconnect.common.enums.StatutTraitementFacture;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Payload unifié pour la validation / validation partielle / rejet d'une facture.
 *
 * Règles :
 *  - VALIDE               → montantRetenu = montantReclame (ou null → forcé)
 *  - VALIDE_PARTIELLEMENT → montantRetenu strictement < montantReclame, motifRejet requis
 *  - REJETE               → montantRetenu = 0, motifRejet requis
 */
public record UpdateFactureRequest(
        @NotNull StatutTraitementFacture statutTraitement,
        BigDecimal montantRetenu,
        String motifRejet
) {}
