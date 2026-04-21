package com.ossanasur.cbconnect.module.statistiques.service;

import com.ossanasur.cbconnect.module.finance.repository.EncaissementRepository;
import com.ossanasur.cbconnect.module.finance.repository.PaiementRepository;
import com.ossanasur.cbconnect.module.sinistre.repository.SinistreRepository;
import com.ossanasur.cbconnect.module.statistiques.dto.CadenceDto;
import com.ossanasur.cbconnect.module.statistiques.dto.EtatFinancierDto;
import com.ossanasur.cbconnect.module.statistiques.dto.EtatSinistreDto;
import com.ossanasur.cbconnect.module.statistiques.dto.EtatFinancierDto.LigneCompagnie;
import com.ossanasur.cbconnect.module.statistiques.dto.EtatFinancierDto.LigneEncaissement;
import com.ossanasur.cbconnect.module.statistiques.dto.EtatFinancierDto.LignePaiement;
import com.ossanasur.cbconnect.module.statistiques.dto.EtatSinistreDto.LigneSinistre;
import com.ossanasur.cbconnect.module.statistiques.dto.ReportingEncaissementDto;
import com.ossanasur.cbconnect.module.statistiques.dto.ReportingMensuelDto;
import com.ossanasur.cbconnect.module.statistiques.dto.ReportingPaiementDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatistiquesService {

    private final SinistreRepository sinistreRepository;
    private final EncaissementRepository encaissementRepository;
    private final PaiementRepository paiementRepository;

    // ─── État I : Sinistres par pays émetteur ────────────────────────

    public EtatSinistreDto etatSinistres(int annee) {
        int n1 = annee - 1;
        List<Object[]> rows = sinistreRepository.statSinistreParPays(annee, n1);

        List<LigneSinistre> lignes = rows.stream().map(r -> {
            String bureau = (String) r[0];
            String codePays = (String) r[1];
            long nbN1 = toLong(r[2]);
            long nbN = toLong(r[3]);
            return new LigneSinistre(bureau, codePays, nbN1, nbN);
        }).toList();

        // Calculer les % par rapport au total N
        long totalN = lignes.stream().mapToLong(LigneSinistre::getNbN).sum();
        lignes.forEach(l -> l.setPourcentage(totalN));

        return new EtatSinistreDto(annee, lignes);
    }

    // ─── État II : Encaissements + Paiements ─────────────────────────

    public EtatFinancierDto etatFinancier(int annee) {
        int n1 = annee - 1;

        // ── Encaissements par pays ──
        List<Object[]> encRows = encaissementRepository.statEncaissementParPays(annee, n1);
        List<LigneEncaissement> encaissements = encRows.stream().map(r -> new LigneEncaissement(
                (String) r[0], (String) r[1],
                toLong(r[2]), toBd(r[3]), toBd(r[4]),
                toLong(r[5]), toBd(r[6]), toBd(r[7]))).toList();

        // ── Paiements par pays ──
        List<Object[]> payRows = paiementRepository.statPaiementParPays(annee, n1);
        List<LignePaiement> paiements = payRows.stream().map(r -> new LignePaiement(
                (String) r[0], (String) r[1],
                toLong(r[2]), toBd(r[3]),
                toLong(r[4]), toBd(r[5]))).toList();

        // Calculer les % montant paiement
        BigDecimal totalMontantN = paiements.stream()
                .map(LignePaiement::getMontantN)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        paiements.forEach(p -> p.setPartMontant(totalMontantN));

        // ── Dont Togo : encaissements par compagnie ──
        List<Object[]> encTogoRows = encaissementRepository.statEncaissementDontTogo(annee, n1);
        List<LigneCompagnie> encTogo = encTogoRows.stream().map(r -> new LigneCompagnie(
                (String) r[0], (String) r[1],
                toLong(r[2]), toBd(r[3]),
                toLong(r[4]), toBd(r[5]))).toList();

        // ── Dont Togo : paiements par compagnie ──
        List<Object[]> payTogoRows = paiementRepository.statPaiementDontTogo(annee, n1);
        List<LigneCompagnie> payTogo = payTogoRows.stream().map(r -> new LigneCompagnie(
                (String) r[0], (String) r[1],
                toLong(r[2]), toBd(r[3]),
                toLong(r[4]), toBd(r[5]))).toList();

        return new EtatFinancierDto(annee, encaissements, paiements, encTogo, payTogo);
    }

    private static final String[] MOIS_FR = {
            "", "JANVIER", "FÉVRIER", "MARS", "AVRIL", "MAI", "JUIN",
            "JUILLET", "AOÛT", "SEPTEMBRE", "OCTOBRE", "NOVEMBRE", "DÉCEMBRE"
    };

    public ReportingMensuelDto reportingMensuel(int annee, int mois) {
        int n1 = annee - 1;

        // ── Tableau 1 : par pays ──────────────────────────────────────
        List<Object[]> paysRows = sinistreRepository.reportingMensuelParPays(annee, n1, mois);

        List<ReportingMensuelDto.LigneReportingPays> parPays = paysRows.stream()
                .map(r -> new ReportingMensuelDto.LigneReportingPays(
                        (String) r[0], (String) r[1],
                        toLong(r[2]), toLong(r[3]),
                        toLong(r[4]), toLong(r[5]),
                        toLong(r[2]) + toLong(r[4]), toLong(r[3]) + toLong(r[5]),
                        toLong(r[6]), toLong(r[7]),
                        toLong(r[8]), toLong(r[9]),
                        toLong(r[6]) + toLong(r[8]), toLong(r[7]) + toLong(r[9]),
                        toLong(r[10])))
                .toList();

        ReportingMensuelDto.LigneReportingPays totalPays = new ReportingMensuelDto.LigneReportingPays(
                "TOTAL", "",
                sum(parPays, l -> l.te_mois_n1()), sum(parPays, l -> l.te_mois_n()),
                sum(parPays, l -> l.et_mois_n1()), sum(parPays, l -> l.et_mois_n()),
                sum(parPays, l -> l.tot_mois_n1()), sum(parPays, l -> l.tot_mois_n()),
                sum(parPays, l -> l.te_cumul_n1()), sum(parPays, l -> l.te_cumul_n()),
                sum(parPays, l -> l.et_cumul_n1()), sum(parPays, l -> l.et_cumul_n()),
                sum(parPays, l -> l.tot_cumul_n1()), sum(parPays, l -> l.tot_cumul_n()),
                sum(parPays, l -> l.tot_fda_n()));

        // ── Tableau 2 : par compagnie ─────────────────────────────────
        List<Object[]> compRows = sinistreRepository.reportingMensuelParCompagnie(annee, n1, mois);

        List<ReportingMensuelDto.LigneReportingCompagnie> parCompagnie = compRows.stream()
                .map(r -> new ReportingMensuelDto.LigneReportingCompagnie(
                        (String) r[0],
                        toLong(r[2]), toLong(r[3]),
                        toLong(r[4]), toLong(r[5]),
                        toLong(r[2]) + toLong(r[4]), toLong(r[3]) + toLong(r[5]),
                        toLong(r[6]), toLong(r[7]),
                        toLong(r[8]), toLong(r[9]),
                        toLong(r[6]) + toLong(r[8]), toLong(r[7]) + toLong(r[9]),
                        toLong(r[10])))
                .toList();

        ReportingMensuelDto.LigneReportingCompagnie totalComp = new ReportingMensuelDto.LigneReportingCompagnie(
                "TOTAL",
                sum(parCompagnie, l -> l.te_mois_n1()), sum(parCompagnie, l -> l.te_mois_n()),
                sum(parCompagnie, l -> l.et_mois_n1()), sum(parCompagnie, l -> l.et_mois_n()),
                sum(parCompagnie, l -> l.tot_mois_n1()), sum(parCompagnie, l -> l.tot_mois_n()),
                sum(parCompagnie, l -> l.te_cumul_n1()), sum(parCompagnie, l -> l.te_cumul_n()),
                sum(parCompagnie, l -> l.et_cumul_n1()), sum(parCompagnie, l -> l.et_cumul_n()),
                sum(parCompagnie, l -> l.tot_cumul_n1()), sum(parCompagnie, l -> l.tot_cumul_n()),
                sum(parCompagnie, l -> l.tot_fda_n()));

        return new ReportingMensuelDto(annee, mois,
                mois >= 1 && mois <= 12 ? MOIS_FR[mois] : "?",
                n1, parPays, totalPays, parCompagnie, totalComp);
    }

    // Helper sum
    private <T> long sum(List<T> list, java.util.function.ToLongFunction<T> fn) {
        return list.stream().mapToLong(fn).sum();
    }

    public ReportingEncaissementDto reportingEncaissements(int annee, int mois) {
        int n1 = annee - 1;

        // ── Tableau I : par pays payeur ──────────────────────────
        List<Object[]> paysRows = encaissementRepository.reportingMensuelEncParPays(annee, n1, mois);

        List<ReportingEncaissementDto.LigneEncPays> parPays = paysRows.stream()
                .map(r -> new ReportingEncaissementDto.LigneEncPays(
                        (String) r[0], (String) r[1],
                        toLong(r[2]), toBd(r[3]),
                        toLong(r[4]), toBd(r[5]),
                        toLong(r[6]), toBd(r[7]),
                        toLong(r[8]), toBd(r[9]),
                        toLong(r[10]), toBd(r[11])))
                .toList();

        ReportingEncaissementDto.LigneEncPays totalPays = new ReportingEncaissementDto.LigneEncPays(
                "TOTAL", "",
                sumL(parPays, l -> l.nb_mois_n1()), sumBd(parPays, l -> l.mt_mois_n1()),
                sumL(parPays, l -> l.nb_mois_n()), sumBd(parPays, l -> l.mt_mois_n()),
                sumL(parPays, l -> l.nb_cumul_n1()), sumBd(parPays, l -> l.mt_cumul_n1()),
                sumL(parPays, l -> l.nb_cumul_n()), sumBd(parPays, l -> l.mt_cumul_n()),
                sumL(parPays, l -> l.nb_fda_n()), sumBd(parPays, l -> l.mt_fda_n()));

        // ── Tableau II : par compagnie membre togolaise ──────────
        List<Object[]> compRows = encaissementRepository.reportingMensuelEncParCompagnie(annee, n1, mois);

        List<ReportingEncaissementDto.LigneEncCompagnie> parComp = compRows.stream()
                .map(r -> new ReportingEncaissementDto.LigneEncCompagnie(
                        (String) r[0],
                        toLong(r[2]), toBd(r[3]),
                        toLong(r[4]), toBd(r[5]),
                        toLong(r[6]), toBd(r[7]),
                        toLong(r[8]), toBd(r[9]),
                        toLong(r[10]), toBd(r[11])))
                .toList();

        ReportingEncaissementDto.LigneEncCompagnie totalComp = new ReportingEncaissementDto.LigneEncCompagnie(
                "TOTAL",
                sumL(parComp, l -> l.nb_mois_n1()), sumBd(parComp, l -> l.mt_mois_n1()),
                sumL(parComp, l -> l.nb_mois_n()), sumBd(parComp, l -> l.mt_mois_n()),
                sumL(parComp, l -> l.nb_cumul_n1()), sumBd(parComp, l -> l.mt_cumul_n1()),
                sumL(parComp, l -> l.nb_cumul_n()), sumBd(parComp, l -> l.mt_cumul_n()),
                sumL(parComp, l -> l.nb_fda_n()), sumBd(parComp, l -> l.mt_fda_n()));

        return new ReportingEncaissementDto(
                annee, mois,
                mois >= 1 && mois <= 12 ? MOIS_FR[mois] : "?",
                n1, parPays, totalPays, parComp, totalComp);
    }

    public ReportingPaiementDto reportingPaiements(int annee, int mois) {
        int n1 = annee - 1;

        // ── Tableau I : par pays bénéficiaire ───────────────────────────
        List<Object[]> paysRows = paiementRepository.reportingMensuelPaiParPays(annee, n1, mois);

        List<ReportingPaiementDto.LignePaiPays> parPays = paysRows.stream()
                .map(r -> new ReportingPaiementDto.LignePaiPays(
                        (String) r[0], (String) r[1],
                        toLong(r[2]), toBd(r[3]),
                        toLong(r[4]), toBd(r[5]),
                        toLong(r[6]), toBd(r[7]),
                        toLong(r[8]), toBd(r[9]),
                        toLong(r[10]), toBd(r[11])))
                .toList();

        ReportingPaiementDto.LignePaiPays totalPays = new ReportingPaiementDto.LignePaiPays(
                "TOTAL", "",
                sumL(parPays, l -> l.nb_mois_n1()), sumBd(parPays, l -> l.mt_mois_n1()),
                sumL(parPays, l -> l.nb_mois_n()), sumBd(parPays, l -> l.mt_mois_n()),
                sumL(parPays, l -> l.nb_cumul_n1()), sumBd(parPays, l -> l.mt_cumul_n1()),
                sumL(parPays, l -> l.nb_cumul_n()), sumBd(parPays, l -> l.mt_cumul_n()),
                sumL(parPays, l -> l.nb_fda_n()), sumBd(parPays, l -> l.mt_fda_n()));

        // ── Tableau II : par compagnie membre togolaise ─────────────────
        List<Object[]> compRows = paiementRepository.reportingMensuelPaiParCompagnie(annee, n1, mois);

        List<ReportingPaiementDto.LignePaiCompagnie> parComp = compRows.stream()
                .map(r -> new ReportingPaiementDto.LignePaiCompagnie(
                        (String) r[0],
                        toLong(r[2]), toBd(r[3]),
                        toLong(r[4]), toBd(r[5]),
                        toLong(r[6]), toBd(r[7]),
                        toLong(r[8]), toBd(r[9]),
                        toLong(r[10]), toBd(r[11])))
                .toList();

        ReportingPaiementDto.LignePaiCompagnie totalComp = new ReportingPaiementDto.LignePaiCompagnie(
                "TOTAL",
                sumL(parComp, l -> l.nb_mois_n1()), sumBd(parComp, l -> l.mt_mois_n1()),
                sumL(parComp, l -> l.nb_mois_n()), sumBd(parComp, l -> l.mt_mois_n()),
                sumL(parComp, l -> l.nb_cumul_n1()), sumBd(parComp, l -> l.mt_cumul_n1()),
                sumL(parComp, l -> l.nb_cumul_n()), sumBd(parComp, l -> l.mt_cumul_n()),
                sumL(parComp, l -> l.nb_fda_n()), sumBd(parComp, l -> l.mt_fda_n()));

        return new ReportingPaiementDto(
                annee, mois,
                mois >= 1 && mois <= 12 ? MOIS_FR[mois] : "?",
                n1, parPays, totalPays, parComp, totalComp);
    }

    // ─── R4 : Cadence de survenance par rapport au paiement ──────────────

    /** Nombre de périodes distinctes dans le triangle (hors "ant"). */
    private static final int NB_PERIODES = 4;

    /**
     * Triangle de cadence de règlement.
     *
     * @param anneeRef Année de référence (colonne la plus récente).
     *                 Les colonnes/lignes couvrent [anneeRef, anneeRef-1,
     *                 anneeRef-2, anneeRef-3, -1("ant")].
     */
    public CadenceDto cadence(int anneeRef) {
        int anneeMin = anneeRef - NB_PERIODES + 1; // ex: 2024-3 = 2021

        // Années colonnes/lignes : [anneeRef, -1, -2, -3, -1(ant)]
        List<Integer> periodes = new ArrayList<>();
        for (int i = 0; i < NB_PERIODES; i++)
            periodes.add(anneeRef - i);
        periodes.add(-1); // "ant"

        // ── Paiements par pays ──────────────────────────────────
        List<Object[]> payPays = paiementRepository.cadenceParPays(anneeMin);
        List<Object[]> decPays = sinistreRepository.sinistresDeclaresParPays(anneeMin);

        List<CadenceDto.BlocCadence> parPays = construireBlocsParPays(payPays, decPays, periodes);

        // ── Paiements par compagnie ─────────────────────────────
        List<Object[]> payComp = paiementRepository.cadenceParCompagnie(anneeMin);
        List<Object[]> decComp = sinistreRepository.sinistresDeclaresParCompagnie(anneeMin);

        List<CadenceDto.BlocCadence> parCompagnie = construireBlocs(payComp, decComp, periodes, false);
        // AUTRES toujours en dernier
        parCompagnie = parCompagnie.stream()
                .sorted((a, b) -> {
                    if ("AUTRES".equals(a.label()))
                        return 1;
                    if ("AUTRES".equals(b.label()))
                        return -1;
                    return a.label().compareTo(b.label());
                }).toList();

        // ── TOTAL (agrégation de tous les blocs parPays) ────────
        CadenceDto.BlocCadence total = aggregerTotal(parPays, periodes);

        return new CadenceDto(anneeRef, periodes, periodes, total, parPays, parCompagnie);
    }

    // ── Construction des blocs par pays ──────────────────────────

    private List<CadenceDto.BlocCadence> construireBlocsParPays(
            List<Object[]> paiements, List<Object[]> declares,
            List<Integer> periodes) {
        return construireBlocs(paiements, declares, periodes, true);
    }

    private List<CadenceDto.BlocCadence> construireBlocs(
            List<Object[]> paiements, List<Object[]> declares,
            List<Integer> periodes, boolean hasCode) {

        // Map: label → (annee_surv → (annee_pay → {nb, montant}))
        Map<String, Map<Integer, Map<Integer, long[]>>> data = new LinkedHashMap<>();
        Map<String, String> codes = new LinkedHashMap<>();

        for (Object[] r : paiements) {
            String label = (String) r[0];
            String code = hasCode ? (String) r[1] : null;
            int surv = ((Number) r[2]).intValue();
            int pay = ((Number) r[3]).intValue();
            long nb = ((Number) r[4]).longValue();
            BigDecimal mt = toBd(r[5]);

            codes.putIfAbsent(label, code);
            data.computeIfAbsent(label, k -> new LinkedHashMap<>())
                    .computeIfAbsent(surv, k -> new LinkedHashMap<>())
                    .put(pay, new long[] { nb, mt.longValue() }); // long pour simplifier
        }

        // Map: label → (annee_surv → nb_declares)
        Map<String, Map<Integer, Long>> declsMap = new HashMap<>();
        for (Object[] r : declares) {
            String label = (String) r[0];
            int surv = ((Number) r[2]).intValue();
            long nb = ((Number) r[3]).longValue();
            declsMap.computeIfAbsent(label, k -> new HashMap<>()).put(surv, nb);
        }

        // Construire les blocs
        List<CadenceDto.BlocCadence> blocs = new ArrayList<>();

        for (Map.Entry<String, Map<Integer, Map<Integer, long[]>>> entry : data.entrySet()) {
            String label = entry.getKey();
            String code = codes.get(label);
            List<CadenceDto.LigneCadence> lignes = new ArrayList<>();

            for (int surv : periodes) {
                Map<Integer, long[]> cellsForSurv = entry.getValue().getOrDefault(surv, Map.of());
                Map<Integer, CadenceDto.CelluleCadence> cellules = new LinkedHashMap<>();

                long totalNb = 0;
                BigDecimal totalMt = BigDecimal.ZERO;

                for (int pay : periodes) {
                    long[] val = cellsForSurv.getOrDefault(pay, new long[] { 0, 0 });
                    cellules.put(pay, new CadenceDto.CelluleCadence(
                            val[0], BigDecimal.valueOf(val[1])));
                    totalNb += val[0];
                    totalMt = totalMt.add(BigDecimal.valueOf(val[1]));
                }

                long nbreDeclares = declsMap.getOrDefault(label, Map.of()).getOrDefault(surv, 0L);
                double taux = nbreDeclares > 0 ? (double) totalNb / nbreDeclares : 0.0;

                lignes.add(new CadenceDto.LigneCadence(
                        surv, cellules, totalNb, totalMt, nbreDeclares, taux));
            }
            blocs.add(new CadenceDto.BlocCadence(label, code, lignes));
        }
        return blocs;
    }

    // ── Agrégation TOTAL ─────────────────────────────────────────

    private CadenceDto.BlocCadence aggregerTotal(
            List<CadenceDto.BlocCadence> blocs, List<Integer> periodes) {

        // Accumulateurs: surv → pay → {nb, montant}
        Map<Integer, Map<Integer, long[]>> acc = new LinkedHashMap<>();
        Map<Integer, Long> decls = new LinkedHashMap<>();

        for (CadenceDto.BlocCadence bloc : blocs) {
            for (CadenceDto.LigneCadence ligne : bloc.lignes()) {
                int surv = ligne.anneeSurvenance();
                decls.merge(surv, ligne.sinistresDeClares(), Long::sum);
                Map<Integer, long[]> accSurv = acc.computeIfAbsent(surv, k -> new LinkedHashMap<>());
                for (Map.Entry<Integer, CadenceDto.CelluleCadence> e : ligne.cellules().entrySet()) {
                    accSurv.merge(e.getKey(),
                            new long[] { e.getValue().nb(),
                                    e.getValue().montant().longValue() },
                            (a, b) -> new long[] { a[0] + b[0], a[1] + b[1] });
                }
            }
        }

        List<CadenceDto.LigneCadence> lignes = new ArrayList<>();
        for (int surv : periodes) {
            Map<Integer, long[]> cellsForSurv = acc.getOrDefault(surv, Map.of());
            Map<Integer, CadenceDto.CelluleCadence> cellules = new LinkedHashMap<>();
            long totalNb = 0;
            BigDecimal totalMt = BigDecimal.ZERO;

            for (int pay : periodes) {
                long[] val = cellsForSurv.getOrDefault(pay, new long[] { 0, 0 });
                cellules.put(pay, new CadenceDto.CelluleCadence(
                        val[0], BigDecimal.valueOf(val[1])));
                totalNb += val[0];
                totalMt = totalMt.add(BigDecimal.valueOf(val[1]));
            }

            long dec = decls.getOrDefault(surv, 0L);
            double tx = dec > 0 ? (double) totalNb / dec : 0.0;
            lignes.add(new CadenceDto.LigneCadence(
                    surv, cellules, totalNb, totalMt, dec, tx));
        }
        return new CadenceDto.BlocCadence("TOTAL", null, lignes);
    }

    // ── Helpers montants ─────────────────────────────────────────
    private <T> BigDecimal sumBd(List<T> list,
            java.util.function.Function<T, BigDecimal> fn) {
        return list.stream().map(fn).filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private <T> long sumL(List<T> list,
            java.util.function.ToLongFunction<T> fn) {
        return list.stream().mapToLong(fn).sum();
    }

    // ─── Helpers ────────────────────────────────────────────────────

    private static long toLong(Object o) {
        if (o == null)
            return 0L;
        if (o instanceof Number n)
            return n.longValue();
        return 0L;
    }

    private static BigDecimal toBd(Object o) {
        if (o == null)
            return BigDecimal.ZERO;
        if (o instanceof BigDecimal bd)
            return bd;
        if (o instanceof Number n)
            return BigDecimal.valueOf(n.doubleValue());
        return BigDecimal.ZERO;
    }
}
