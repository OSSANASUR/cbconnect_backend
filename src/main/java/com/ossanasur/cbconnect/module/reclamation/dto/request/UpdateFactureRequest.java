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
 *
 * montantReclame : si fourni et que la facture a été créée avec montantReclame = 0
 * (upload GED auto), met à jour le montant réclamé avant d'appliquer le traitement.
 */
public record UpdateFactureRequest(
        @NotNull StatutTraitementFacture statutTraitement,
        BigDecimal montantRetenu,
        String motifRejet,
        BigDecimal montantReclame
) {}
