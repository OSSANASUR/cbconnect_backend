package com.ossanasur.cbconnect.module.statistiques.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Reporting mensuel BNCB-TG — Tableaux comparatifs des sinistres payés (R3).
 *
 * Tableau I : par pays bénéficiaire (BÉNIN, BURKINA, CI, GHANA, MALI, NIGER,
 * NIGERIA, TOGO*)
 * Tableau II : par compagnie membre togolaise (marché togolais)
 *
 * Chaque ligne expose : nb + montant pour mois N-1/N, cumul N-1/N, fin d'année
 * N.
 */
public record ReportingPaiementDto(

        int annee,
        int mois,
        String libelleMois,
        int anneeRef,

        List<LignePaiPays> parPays,
        LignePaiPays totalPays,

        List<LignePaiCompagnie> parCompagnie,
        LignePaiCompagnie totalCompagnie) {

    // ── Ligne Tableau I : par pays bénéficiaire ───────────────────
    public record LignePaiPays(
            String pays,
            String codePays,
            // MOIS
            long nb_mois_n1, BigDecimal mt_mois_n1,
            long nb_mois_n, BigDecimal mt_mois_n,
            // CUMUL
            long nb_cumul_n1, BigDecimal mt_cumul_n1,
            long nb_cumul_n, BigDecimal mt_cumul_n,
            // FIN D'ANNÉE
            long nb_fda_n, BigDecimal mt_fda_n) {
    }

    // ── Ligne Tableau II : par compagnie membre togolaise ─────────
    public record LignePaiCompagnie(
            String compagnie,
            long nb_mois_n1, BigDecimal mt_mois_n1,
            long nb_mois_n, BigDecimal mt_mois_n,
            long nb_cumul_n1, BigDecimal mt_cumul_n1,
            long nb_cumul_n, BigDecimal mt_cumul_n,
            long nb_fda_n, BigDecimal mt_fda_n) {
    }
}