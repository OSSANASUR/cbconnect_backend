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
