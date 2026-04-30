package com.ossanasur.cbconnect.module.statistiques.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

// ═══════════════════════════════════════════════════════════════════
//  DTOs — États statistiques BNCB Togo
// ═══════════════════════════════════════════════════════════════════

/**
 * État I — Sinistres déclarés par pays émetteur (comparaison N-1 vs N).
 */
public class EtatSinistreDto {

    private int annee;
    private List<LigneSinistre> lignes;
    private LigneSinistre total;
    private List<LigneEvolution> evolution;

    // ── Constructeur / getters ────────────────────────────────────────
    public EtatSinistreDto() {
    }

    public EtatSinistreDto(int annee, List<LigneSinistre> lignes) {
        this.annee = annee;
        this.lignes = lignes;
        this.total = calculerTotal(lignes);
    }

    public int getAnnee() {
        return annee;
    }

    public List<LigneSinistre> getLignes() {
        return lignes;
    }

    public LigneSinistre getTotal() {
        return total;
    }

    public List<LigneEvolution> getEvolution() {
        return evolution;
    }

    public void setEvolution(List<LigneEvolution> evolution) {
        this.evolution = evolution;
    }

    private static LigneSinistre calculerTotal(List<LigneSinistre> lignes) {
        long nbN1 = lignes.stream().mapToLong(LigneSinistre::getNbN1).sum();
        long nbN = lignes.stream().mapToLong(LigneSinistre::getNbN).sum();
        return new LigneSinistre("TOTAL", null, nbN1, nbN);
    }

    public static class LigneSinistre {
        private String bureau;
        private String codePays;
        private long nbN1;
        private long nbN;
        private Double variation; // (N - N-1) / N-1
        private Double pourcentage; // N / totalN

        public LigneSinistre() {
        }

        public LigneSinistre(String bureau, String codePays, long nbN1, long nbN) {
            this.bureau = bureau;
            this.codePays = codePays;
            this.nbN1 = nbN1;
            this.nbN = nbN;
            this.variation = nbN1 == 0 ? null : round((double) (nbN - nbN1) / nbN1 * 100);
        }

        public void setPourcentage(long totalN) {
            this.pourcentage = totalN == 0 ? 0.0 : round((double) nbN / totalN * 100);
        }

        public String getBureau() {
            return bureau;
        }

        public String getCodePays() {
            return codePays;
        }

        public long getNbN1() {
            return nbN1;
        }

        public long getNbN() {
            return nbN;
        }

        public Double getVariation() {
            return variation;
        }

        public Double getPourcentage() {
            return pourcentage;
        }

        private static double round(double v) {
            return BigDecimal.valueOf(v).setScale(1, RoundingMode.HALF_UP).doubleValue();
        }
    }

    public static class LigneEvolution {
        private int annee;
        private long total;
        private long et;
        private long te;

        public LigneEvolution(int annee, long total, long et, long te) {
            this.annee = annee;
            this.total = total;
            this.et    = et;
            this.te    = te;
        }

        public int getAnnee()  { return annee; }
        public long getTotal() { return total; }
        public long getEt()    { return et; }
        public long getTe()    { return te; }
    }
}
