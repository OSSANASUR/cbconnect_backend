package com.ossanasur.cbconnect.module.statistiques.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Graphique Encaissements vs Paiements — pluriannuel.
 *
 * Pour chaque année dans la plage demandée, on expose :
 * - nb et montant des encaissements
 * - nb et montant des paiements
 *
 * Utilisé pour construire un graphique en barres groupées côté frontend.
 */
public record GraphiqueEncPaiDto(

        int anneeDebut,
        int anneeFin,

        /** Une entrée par année, triée cronologiquement. */
        List<LigneAnnuelle> series

) {
    public record LigneAnnuelle(
            int annee,
            long nbEncaissements,
            BigDecimal montantEncaissements,
            long nbPaiements,
            BigDecimal montantPaiements) {
    }
}