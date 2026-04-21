package com.ossanasur.cbconnect.module.statistiques.service;

import com.ossanasur.cbconnect.module.finance.repository.EncaissementRepository;
import com.ossanasur.cbconnect.module.finance.repository.PaiementRepository;
import com.ossanasur.cbconnect.module.sinistre.repository.SinistreRepository;
import com.ossanasur.cbconnect.module.statistiques.dto.EtatFinancierDto;
import com.ossanasur.cbconnect.module.statistiques.dto.EtatSinistreDto;
import com.ossanasur.cbconnect.module.statistiques.dto.EtatFinancierDto.LigneCompagnie;
import com.ossanasur.cbconnect.module.statistiques.dto.EtatFinancierDto.LigneEncaissement;
import com.ossanasur.cbconnect.module.statistiques.dto.EtatFinancierDto.LignePaiement;
import com.ossanasur.cbconnect.module.statistiques.dto.EtatSinistreDto.LigneSinistre;
import com.ossanasur.cbconnect.module.statistiques.dto.ReportingEncaissementDto;
import com.ossanasur.cbconnect.module.statistiques.dto.ReportingMensuelDto;

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
