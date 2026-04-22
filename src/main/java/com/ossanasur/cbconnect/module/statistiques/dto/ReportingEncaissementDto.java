package com.ossanasur.cbconnect.module.statistiques.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Reporting mensuel BNCB-TG — Tableaux comparatifs des sinistres encaissés (R2).
 *
 * Tableau I  : par pays payeur (BÉNIN, BURKINA, CI, GHANA, MALI, NIGER, NIGERIA, TOGO*)
 * Tableau II : par compagnie membre togolaise (marché togolais)
 *
 * Chaque ligne expose : nb + montant pour mois N-1/N, cumul N-1/N, fin d'année N.
 */
public record ReportingEncaissementDto(

        int    annee,
        int    mois,
        String libelleMois,
        int    anneeRef,

        List<LigneEncPays>      parPays,
        LigneEncPays            totalPays,

        List<LigneEncCompagnie> parCompagnie,
        LigneEncCompagnie       totalCompagnie
) {

    // ── Ligne Tableau I : par pays payeur ─────────────────────────
    public record LigneEncPays(
            String     pays,
            String     codePays,
            // MOIS
            long       nb_mois_n1,   BigDecimal mt_mois_n1,
            long       nb_mois_n,    BigDecimal mt_mois_n,
            // CUMUL
            long       nb_cumul_n1,  BigDecimal mt_cumul_n1,
            long       nb_cumul_n,   BigDecimal mt_cumul_n,
            // FIN D'ANNÉE
            long       nb_fda_n,     BigDecimal mt_fda_n
    ) {}

    // ── Ligne Tableau II : par compagnie membre togolaise ─────────
    public record LigneEncCompagnie(
            String     compagnie,
            long       nb_mois_n1,   BigDecimal mt_mois_n1,
            long       nb_mois_n,    BigDecimal mt_mois_n,
            long       nb_cumul_n1,  BigDecimal mt_cumul_n1,
            long       nb_cumul_n,   BigDecimal mt_cumul_n,
            long       nb_fda_n,     BigDecimal mt_fda_n
    ) {}
}
