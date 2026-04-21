package com.ossanasur.cbconnect.module.statistiques.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Triangle de cadence de règlement — SINISTRES PAR EXERCICE DE SURVENANCE.
 *
 * Axe X (colonnes) = année où le paiement a été effectué (date_paiement).
 * Axe Y (lignes) = année où l'accident est survenu (date_accident).
 *
 * Les années antérieures à anneeMin sont regroupées sous la clé -1 ("ant").
 *
 * Rendu : TOTAL, par pays (pays_emetteur), par compagnie membre togolaise.
 */
public record CadenceDto(

                /** Année de référence (colonne la plus récente). */
                int anneeRef,

                /**
                 * Années utilisées comme colonnes "payés en", du plus récent au plus ancien.
                 * Ex : [2024, 2023, 2022, 2021, -1] (-1 = "2020 et ant.")
                 */
                List<Integer> anneesColonnes,

                /**
                 * Années utilisées comme lignes "survenus en", du plus récent au plus ancien.
                 * Identique à anneesColonnes dans ce rapport.
                 */
                List<Integer> anneesSurvenance,

                BlocCadence total,
                List<BlocCadence> parPays,
                List<BlocCadence> parCompagnie) {

        // ── Bloc = un pays ou une compagnie ────────────────────────────────
        public record BlocCadence(
                        String label, // "BENIN", "FIDELIA TG", "TOTAL"
                        String code, // code pays ou null
                        List<LigneCadence> lignes // une ligne par année de survenance
        ) {
        }

        // ── Ligne = une année de survenance ───────────────────────────────
        public record LigneCadence(
                        /** Année de survenance ; -1 = regroupement "ant" */
                        int anneeSurvenance,

                        /**
                         * Paiements par année de paiement.
                         * Clé = année (-1 pour "ant"), valeur = {nb, montant}.
                         */
                        Map<Integer, CelluleCadence> cellules,

                        long nbTotal,
                        BigDecimal montantTotal,

                        /** Sinistres déclarés pour cette année de survenance (toutes issues). */
                        long sinistresDeClares,

                        /** tauxPaye = nbTotal / sinistresDeClares (0 si sinistresDeClares = 0). */
                        double tauxPaye) {
        }

        // ── Cellule = intersection (survenance, paiement) ─────────────────
        public record CelluleCadence(
                        long nb,
                        BigDecimal montant) {
        }
}
