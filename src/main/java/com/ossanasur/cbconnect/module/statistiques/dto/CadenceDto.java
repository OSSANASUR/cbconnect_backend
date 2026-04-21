package com.ossanasur.cbconnect.module.statistiques.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * État de cadence de survenance par rapport au paiement — R4.
 *
 * Structure identique au fichier Excel R4_Cadence.xlsx :
 * - Tableau TOTAL : vue consolidée tous pays
 * - Tableau TOGO : sinistres gérés par le Togo (par pays émetteur)
 * - Tableau par compagnie togolaise (FIDELIA, GTA, NSIA, POOL, SANLAM, SUNU…)
 *
 * Pour chaque bloc :
 * Lignes = exercice de survenance (année de l'accident)
 * Colonnes = exercice de paiement (année du paiement)
 * Cellule = nb sinistres payés ET montant total payé
 *
 * Le "taux payé" = montant payé / montant total des sinistres déclarés.
 */
public record CadenceDto(

        int anneeRef, // année du reporting (ex: 2026)
        String libelleMois, // "JANVIER", "MARS", etc.
        int mois,

        // ── Colonnes d'exercices de paiement disponibles ─────────────────
        // Exemple : [2020, 2021, 2022, 2023, 2024, 2025, 2026]
        List<Integer> exercicesPaiement,

        // ── Blocs ─────────────────────────────────────────────────────────
        BlocCadence total,
        List<BlocCadence> parPays,
        List<BlocCadence> parCompagnieTogo

) {

    /**
     * Bloc cadence pour un pays ou une compagnie.
     *
     * lignes : une ligne par exercice de survenance
     * totalLignes: ligne de synthèse (toutes survenances confondues)
     */
    public record BlocCadence(
            String libelle, // "TOTAL", "TOGO", "FIDELIA", etc.
            String codePays, // code carte brune, null pour compagnies

            List<LigneCadence> lignes,
            LigneCadence totalLignes) {
    }

    /**
     * Ligne : exercice de survenance donné.
     *
     * cellules : liste ordonnée de cellules, une par exercice de paiement.
     * La position i correspond à exercicesPaiement.get(i).
     */
    public record LigneCadence(
            String exerciceSurvenance, // "2020+ant", "2021", "2022", etc.
            int anneeAccident, // -1 pour la ligne "2020+ant"

            List<CelluleCadence> cellules,

            // Totaux de la ligne (toutes années de paiement)
            long totalNb,
            BigDecimal totalMontant,

            // Sinistres déclarés pour cet exercice (pour calcul taux payé)
            long nbDeclares,
            BigDecimal montantDeclares) {
    }

    /**
     * Cellule : intersection (survenance X, paiement Y).
     */
    public record CelluleCadence(
            int exercicePaiement,
            long nb,
            BigDecimal montant) {
    }
}