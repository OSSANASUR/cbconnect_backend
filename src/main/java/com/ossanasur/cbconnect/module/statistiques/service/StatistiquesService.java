package com.ossanasur.cbconnect.module.statistiques.service;

import com.ossanasur.cbconnect.module.finance.repository.EncaissementRepository;
import com.ossanasur.cbconnect.module.finance.repository.PaiementRepository;
import com.ossanasur.cbconnect.module.sinistre.repository.SinistreRepository;
import com.ossanasur.cbconnect.module.statistiques.dto.CadenceDto;
import com.ossanasur.cbconnect.module.statistiques.dto.CadenceDto.BlocCadence;
import com.ossanasur.cbconnect.module.statistiques.dto.CadenceDto.CelluleCadence;
import com.ossanasur.cbconnect.module.statistiques.dto.CadenceDto.LigneCadence;
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
import java.util.List;

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

    public CadenceDto cadenceSurvenance(int annee, int mois) {

        String libelleMois = (mois >= 1 && mois <= 12) ? MOIS_FR[mois] : "?";
        int anneeMin = annee - 5; // on remonte sur 5 exercices + "2020+ant"

        // ── Bloc TOTAL ───────────────────────────────────────────────────
        List<Object[]> totalRows = paiementRepository.cadenceTotal(annee);
        List<Integer> exercicesPaiement = buildExercicesPaiement(totalRows, anneeMin, annee, 2);

        BlocCadence blocTotal = buildBloc("TOTAL", null, totalRows, exercicesPaiement, anneeMin, annee);

        // ── Par pays ─────────────────────────────────────────────────────
        List<Object[]> paysRows = paiementRepository.cadenceParPays(annee);
        List<BlocCadence> parPays = groupByEntity(paysRows, exercicesPaiement, anneeMin, annee,
                r -> (String) r[0], r -> (String) r[1],
                r -> ((Number) r[2]).intValue(), r -> ((Number) r[3]).intValue(),
                r -> toLong(r[4]), r -> toBd(r[5]));

        // ── Par compagnie togolaise ───────────────────────────────────────
        List<Object[]> compRows = paiementRepository.cadenceParCompagnieTogo(annee);
        List<BlocCadence> parCompagnieTogo = groupByEntity(compRows, exercicesPaiement, anneeMin, annee,
                r -> (String) r[0], r -> null,
                r -> ((Number) r[1]).intValue(), r -> ((Number) r[2]).intValue(),
                r -> toLong(r[3]), r -> toBd(r[4]));

        return new CadenceDto(annee, libelleMois, mois, exercicesPaiement,
                blocTotal, parPays, parCompagnieTogo);
    }

    /**
     * Déduit la liste des exercices de paiement à partir des données brutes.
     * On prend toutes les années trouvées dans les données + l'année courante,
     * filtrées entre anneeMin et anneeMax.
     */
    private List<Integer> buildExercicesPaiement(List<Object[]> rows, int anneeMin, int anneeMax, int paiColIdx) {
        java.util.TreeSet<Integer> annees = new java.util.TreeSet<>();
        for (Object[] r : rows) {
            int ap = ((Number) r[paiColIdx]).intValue();
            if (ap >= anneeMin && ap <= anneeMax)
                annees.add(ap);
        }
        // Garantir que l'année courante est présente
        annees.add(anneeMax);
        return new java.util.ArrayList<>(annees);
    }

    /**
     * Construit un BlocCadence TOTAL à partir de rows brutes de cadenceTotal().
     * rows : [0]=annee_survenance [1]=annee_paiement [2]=nb [3]=montant
     */
    private BlocCadence buildBloc(String libelle, String codePays,
            List<Object[]> rows,
            List<Integer> exercicesPaiement,
            int anneeMin, int anneeMax) {

        // Grouper par annee_survenance
        java.util.Map<Integer, List<Object[]>> bySurv = new java.util.LinkedHashMap<>();
        for (Object[] r : rows) {
            int as = ((Number) r[0]).intValue();
            bySurv.computeIfAbsent(as, k -> new java.util.ArrayList<>()).add(r);
        }

        List<LigneCadence> lignes = buildLignes(bySurv, exercicesPaiement, anneeMin, anneeMax, 1, 2, 3);
        LigneCadence total = buildTotalLigne(lignes, exercicesPaiement);
        return new BlocCadence(libelle, codePays, lignes, total);
    }

    /**
     * Regroupe un résultat multi-entités (pays ou compagnie) en liste de
     * BlocCadence.
     */
    private <T> List<BlocCadence> groupByEntity(
            List<Object[]> rows,
            List<Integer> exercicesPaiement,
            int anneeMin, int anneeMax,
            java.util.function.Function<Object[], String> getLibelle,
            java.util.function.Function<Object[], String> getCode,
            java.util.function.Function<Object[], Integer> getSurv,
            java.util.function.Function<Object[], Integer> getPai,
            java.util.function.Function<Object[], Long> getNb,
            java.util.function.Function<Object[], java.math.BigDecimal> getMt) {

        // Grouper par entité
        java.util.LinkedHashMap<String, List<Object[]>> byEntity = new java.util.LinkedHashMap<>();
        java.util.Map<String, String> codeByLib = new java.util.LinkedHashMap<>();
        for (Object[] r : rows) {
            String lib = getLibelle.apply(r);
            byEntity.computeIfAbsent(lib, k -> new java.util.ArrayList<>()).add(r);
            codeByLib.putIfAbsent(lib, getCode.apply(r));
        }

        List<BlocCadence> blocs = new java.util.ArrayList<>();
        for (var entry : byEntity.entrySet()) {
            String lib = entry.getKey();
            List<Object[]> entityRows = entry.getValue();

            // Remap en [survenance, paiement, nb, montant]
            java.util.Map<Integer, List<Object[]>> bySurv = new java.util.LinkedHashMap<>();
            for (Object[] r : entityRows) {
                int as = getSurv.apply(r);
                // Créer un tableau normalisé [survenance, paiement, nb, montant]
                Object[] norm = new Object[] { as, getPai.apply(r), getNb.apply(r), getMt.apply(r) };
                bySurv.computeIfAbsent(as, k -> new java.util.ArrayList<>()).add(norm);
            }

            List<LigneCadence> lignes = buildLignes(bySurv, exercicesPaiement, anneeMin, anneeMax, 1, 2, 3);
            LigneCadence total = buildTotalLigne(lignes, exercicesPaiement);
            blocs.add(new BlocCadence(lib, codeByLib.get(lib), lignes, total));
        }
        return blocs;
    }

    /**
     * Construit les lignes (une par exercice de survenance) à partir d'un map
     * survenance → rows normalisés [s, paiement, nb, montant].
     */
    private List<LigneCadence> buildLignes(
            java.util.Map<Integer, List<Object[]>> bySurv,
            List<Integer> exercicesPaiement,
            int anneeMin, int anneeMax,
            int paiIdx, int nbIdx, int mtIdx) {

        List<LigneCadence> lignes = new java.util.ArrayList<>();

        // Regrouper les survenues < anneeMin dans "anneeMin-1+ant"
        java.util.Map<Integer, List<Object[]>> bySurvClean = new java.util.LinkedHashMap<>();
        List<Object[]> antRows = new java.util.ArrayList<>();
        for (var e : bySurv.entrySet()) {
            if (e.getKey() < anneeMin)
                antRows.addAll(e.getValue());
            else
                bySurvClean.put(e.getKey(), e.getValue());
        }

        // Ligne "2020+ant" (années antérieures regroupées)
        if (!antRows.isEmpty()) {
            lignes.add(buildLigne(anneeMin - 1 + "+ant", -1, antRows,
                    exercicesPaiement, paiIdx, nbIdx, mtIdx));
        }

        // Lignes par année de survenance, ordre croissant
        new java.util.TreeMap<>(bySurvClean).forEach((as, r) -> lignes.add(buildLigne(String.valueOf(as), as, r,
                exercicesPaiement, paiIdx, nbIdx, mtIdx)));

        return lignes;
    }

    private LigneCadence buildLigne(String libelle, int anneeAcc, List<Object[]> rows,
            List<Integer> exercicesPaiement,
            int paiIdx, int nbIdx, int mtIdx) {
        // Index par exercice de paiement
        java.util.Map<Integer, long[]> cellMap = new java.util.HashMap<>();
        java.util.Map<Integer, java.math.BigDecimal[]> cellMtMap = new java.util.HashMap<>();
        for (Object[] r : rows) {
            int ap = ((Number) r[paiIdx]).intValue();
            cellMap.merge(ap, new long[] { toLong(r[nbIdx]) }, (a, b) -> new long[] { a[0] + b[0] });
            cellMtMap.merge(ap,
                    new java.math.BigDecimal[] { toBd(r[mtIdx]) },
                    (a, b) -> new java.math.BigDecimal[] { a[0].add(b[0]) });
        }

        List<CelluleCadence> cellules = exercicesPaiement.stream().map(ep -> new CelluleCadence(ep,
                cellMap.containsKey(ep) ? cellMap.get(ep)[0] : 0L,
                cellMtMap.containsKey(ep) ? cellMtMap.get(ep)[0] : java.math.BigDecimal.ZERO)).toList();

        long totalNb = cellules.stream().mapToLong(CelluleCadence::nb).sum();
        java.math.BigDecimal totalMt = cellules.stream()
                .map(CelluleCadence::montant)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        return new LigneCadence(libelle, anneeAcc, cellules, totalNb, totalMt, 0L, java.math.BigDecimal.ZERO);
    }

    private LigneCadence buildTotalLigne(List<LigneCadence> lignes, List<Integer> exercicesPaiement) {
        List<CelluleCadence> cellules = new java.util.ArrayList<>();
        for (int i = 0; i < exercicesPaiement.size(); i++) {
            final int fi = i;
            long nb = lignes.stream().mapToLong(l -> l.cellules().get(fi).nb()).sum();
            java.math.BigDecimal mt = lignes.stream()
                    .map(l -> l.cellules().get(fi).montant())
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            cellules.add(new CelluleCadence(exercicesPaiement.get(i), nb, mt));
        }
        long totalNb = cellules.stream().mapToLong(CelluleCadence::nb).sum();
        java.math.BigDecimal totalMt = cellules.stream()
                .map(CelluleCadence::montant)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        return new LigneCadence("TOTAL", -1, cellules, totalNb, totalMt, 0L, java.math.BigDecimal.ZERO);
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
