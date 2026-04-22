package com.ossanasur.cbconnect.module.statistiques.dto;

import java.util.List;

/**
 * Reporting mensuel BNCB-TG — Tableaux comparatifs des sinistres enregistrés.
 *
 * Structure identique au fichier Excel R1.xlsx :
 *   • Tableau 1 : par pays partenaire CEDEAO
 *   • Tableau 2 : par compagnie membre togolaise (CIMA RC Auto)
 *
 * Pour chaque ligne : MOIS courant (N-1 vs N) + CUMUL (jan→mois) + FIN D'ANNÉE
 */
public record ReportingMensuelDto(

        int annee,
        int mois,
        String libelleMois,        // "MARS", "JANVIER", etc.
        int anneeRef,              // annee - 1

        List<LigneReportingPays>      parPays,
        LigneReportingPays            totalPays,

        List<LigneReportingCompagnie> parCompagnie,
        LigneReportingCompagnie       totalCompagnie
) {

    // ── Ligne tableau 1 : par pays ────────────────────────────────────
    public record LigneReportingPays(
            String pays,
            String codePays,

            // MOIS courant
            long te_mois_n1,   long te_mois_n,
            long et_mois_n1,   long et_mois_n,
            long tot_mois_n1,  long tot_mois_n,

            // CUMUL jan → mois
            long te_cumul_n1,  long te_cumul_n,
            long et_cumul_n1,  long et_cumul_n,
            long tot_cumul_n1, long tot_cumul_n,

            // FIN D'ANNÉE (jan → dec année N)
            long tot_fda_n
    ) {}

    // ── Ligne tableau 2 : par compagnie membre ────────────────────────
    public record LigneReportingCompagnie(
            String compagnie,

            // MOIS courant
            long te_mois_n1,   long te_mois_n,
            long et_mois_n1,   long et_mois_n,
            long tot_mois_n1,  long tot_mois_n,

            // CUMUL jan → mois
            long te_cumul_n1,  long te_cumul_n,
            long et_cumul_n1,  long et_cumul_n,
            long tot_cumul_n1, long tot_cumul_n,

            // FIN D'ANNÉE
            long tot_fda_n
    ) {}
}
