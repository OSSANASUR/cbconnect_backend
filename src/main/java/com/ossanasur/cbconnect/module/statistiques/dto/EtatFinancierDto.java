package com.ossanasur.cbconnect.module.statistiques.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * État II — Encaissements + Paiements (comparaison N-1 vs N).
 * Contient aussi la décomposition "Dont Togo" par compagnie.
 */
public class EtatFinancierDto {

    private int annee;

    // ── Encaissements par pays émetteur ───────────────────────────────
    private List<LigneEncaissement> encaissements;
    private LigneEncaissement totalEncaissements;

    // ── Paiements par pays émetteur ───────────────────────────────────
    private List<LignePaiement> paiements;
    private LignePaiement totalPaiements;

    // ── Détail Togo ───────────────────────────────────────────────────
    private List<LigneCompagnie> encaissementsTogo;
    private List<LigneCompagnie> paiementsTogo;

    public EtatFinancierDto() {
    }

    public EtatFinancierDto(int annee,
            List<LigneEncaissement> encaissements,
            List<LignePaiement> paiements,
            List<LigneCompagnie> encaissementsTogo,
            List<LigneCompagnie> paiementsTogo) {
        this.annee = annee;
        this.encaissements = encaissements;
        this.totalEncaissements = totalEncaissement(encaissements);
        this.paiements = paiements;
        this.totalPaiements = totalPaiement(paiements);
        this.encaissementsTogo = encaissementsTogo;
        this.paiementsTogo = paiementsTogo;
    }

    // getters
    public int getAnnee() {
        return annee;
    }

    public List<LigneEncaissement> getEncaissements() {
        return encaissements;
    }

    public LigneEncaissement getTotalEncaissements() {
        return totalEncaissements;
    }

    public List<LignePaiement> getPaiements() {
        return paiements;
    }

    public LignePaiement getTotalPaiements() {
        return totalPaiements;
    }

    public List<LigneCompagnie> getEncaissementsTogo() {
        return encaissementsTogo;
    }

    public List<LigneCompagnie> getPaiementsTogo() {
        return paiementsTogo;
    }

    // ── Ligne encaissement ────────────────────────────────────────────
    public static class LigneEncaissement {
        private String bureau;
        private String codePays;
        // N-1
        private long nbN1;
        private BigDecimal montantN1;
        private BigDecimal pgN1;
        // N
        private long nbN;
        private BigDecimal montantN;
        private BigDecimal pgN;
        // Variations
        private Double varNb;
        private Double varMontant;
        private Double varPg;

        public LigneEncaissement() {
        }

        public LigneEncaissement(String bureau, String codePays,
                long nbN1, BigDecimal montantN1, BigDecimal pgN1,
                long nbN, BigDecimal montantN, BigDecimal pgN) {
            this.bureau = bureau;
            this.codePays = codePays;
            this.nbN1 = nbN1;
            this.montantN1 = coalesce(montantN1);
            this.pgN1 = coalesce(pgN1);
            this.nbN = nbN;
            this.montantN = coalesce(montantN);
            this.pgN = coalesce(pgN);
            this.varNb = pct(nbN1, nbN);
            this.varMontant = pct(montantN1, montantN);
            this.varPg = pct(pgN1, pgN);
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

        public BigDecimal getMontantN1() {
            return montantN1;
        }

        public BigDecimal getPgN1() {
            return pgN1;
        }

        public long getNbN() {
            return nbN;
        }

        public BigDecimal getMontantN() {
            return montantN;
        }

        public BigDecimal getPgN() {
            return pgN;
        }

        public Double getVarNb() {
            return varNb;
        }

        public Double getVarMontant() {
            return varMontant;
        }

        public Double getVarPg() {
            return varPg;
        }
    }

    // ── Ligne paiement ────────────────────────────────────────────────
    public static class LignePaiement {
        private String beneficiaire;
        private String codePays;
        private long nbN1;
        private BigDecimal montantN1;
        private long nbN;
        private BigDecimal montantN;
        private Double varNb;
        private Double varMontant;
        private Double partMontant; // montantN / totalMontantN

        public LignePaiement() {
        }

        public LignePaiement(String beneficiaire, String codePays,
                long nbN1, BigDecimal montantN1,
                long nbN, BigDecimal montantN) {
            this.beneficiaire = beneficiaire;
            this.codePays = codePays;
            this.nbN1 = nbN1;
            this.montantN1 = coalesce(montantN1);
            this.nbN = nbN;
            this.montantN = coalesce(montantN);
            this.varNb = pct(nbN1, nbN);
            this.varMontant = pct(montantN1, montantN);
        }

        public void setPartMontant(BigDecimal totalMontantN) {
            this.partMontant = totalMontantN.compareTo(BigDecimal.ZERO) == 0 ? 0.0
                    : round(montantN.divide(totalMontantN, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue());
        }

        public String getBeneficiaire() {
            return beneficiaire;
        }

        public String getCodePays() {
            return codePays;
        }

        public long getNbN1() {
            return nbN1;
        }

        public BigDecimal getMontantN1() {
            return montantN1;
        }

        public long getNbN() {
            return nbN;
        }

        public BigDecimal getMontantN() {
            return montantN;
        }

        public Double getVarNb() {
            return varNb;
        }

        public Double getVarMontant() {
            return varMontant;
        }

        public Double getPartMontant() {
            return partMontant;
        }
    }

    // ── Ligne compagnie (dont Togo) ───────────────────────────────────
    public static class LigneCompagnie {
        private String compagnie;
        private String code;
        private long nbN1;
        private BigDecimal montantN1;
        private long nbN;
        private BigDecimal montantN;

        public LigneCompagnie() {
        }

        public LigneCompagnie(String compagnie, String code,
                long nbN1, BigDecimal montantN1,
                long nbN, BigDecimal montantN) {
            this.compagnie = compagnie;
            this.code = code;
            this.nbN1 = nbN1;
            this.montantN1 = coalesce(montantN1);
            this.nbN = nbN;
            this.montantN = coalesce(montantN);
        }

        public String getCompagnie() {
            return compagnie;
        }

        public String getCode() {
            return code;
        }

        public long getNbN1() {
            return nbN1;
        }

        public BigDecimal getMontantN1() {
            return montantN1;
        }

        public long getNbN() {
            return nbN;
        }

        public BigDecimal getMontantN() {
            return montantN;
        }
    }

    // ── Helpers statiques ─────────────────────────────────────────────
    private static LigneEncaissement totalEncaissement(List<LigneEncaissement> l) {
        long n1 = l.stream().mapToLong(LigneEncaissement::getNbN1).sum();
        long n = l.stream().mapToLong(LigneEncaissement::getNbN).sum();
        BigDecimal m1 = l.stream().map(LigneEncaissement::getMontantN1).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal m = l.stream().map(LigneEncaissement::getMontantN).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal p1 = l.stream().map(LigneEncaissement::getPgN1).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal p = l.stream().map(LigneEncaissement::getPgN).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new LigneEncaissement("TOTAL", null, n1, m1, p1, n, m, p);
    }

    private static LignePaiement totalPaiement(List<LignePaiement> l) {
        long n1 = l.stream().mapToLong(LignePaiement::getNbN1).sum();
        long n = l.stream().mapToLong(LignePaiement::getNbN).sum();
        BigDecimal m1 = l.stream().map(LignePaiement::getMontantN1).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal m = l.stream().map(LignePaiement::getMontantN).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new LignePaiement("TOTAL", null, n1, m1, n, m);
    }

    private static BigDecimal coalesce(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static Double pct(long ancien, long nouveau) {
        if (ancien == 0)
            return null;
        return round((double) (nouveau - ancien) / ancien * 100);
    }

    private static Double pct(BigDecimal ancien, BigDecimal nouveau) {
        if (ancien == null || ancien.compareTo(BigDecimal.ZERO) == 0)
            return null;
        return round(nouveau.subtract(ancien)
                .divide(ancien, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue());
    }

    private static double round(double v) {
        return BigDecimal.valueOf(v).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}