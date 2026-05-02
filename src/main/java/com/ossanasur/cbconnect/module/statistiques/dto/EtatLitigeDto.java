package com.ossanasur.cbconnect.module.statistiques.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * État des sinistres en litige — Contentieux et Arbitrage.
 */
public class EtatLitigeDto {

    private int annee; // 0 = tous les exercices
    private List<LigneLitige> lignes;
    private LigneLitige total;
    private List<DossierLitige> dossiers;

    public EtatLitigeDto() {}

    public EtatLitigeDto(int annee, List<LigneLitige> lignes, List<DossierLitige> dossiers) {
        this.annee = annee;
        this.lignes = lignes;
        this.dossiers = dossiers;
        this.total = calculerTotal(lignes);
    }

    public int getAnnee() { return annee; }
    public List<LigneLitige> getLignes() { return lignes; }
    public LigneLitige getTotal() { return total; }
    public List<DossierLitige> getDossiers() { return dossiers; }

    private static LigneLitige calculerTotal(List<LigneLitige> lignes) {
        long nbC = lignes.stream().mapToLong(LigneLitige::getNbContentieux).sum();
        long nbA = lignes.stream().mapToLong(LigneLitige::getNbArbitrage).sum();
        LigneLitige t = new LigneLitige("TOTAL", null, nbC, nbA);
        t.setPourcentage(nbC + nbA);
        return t;
    }

    // ── Ligne récapitulative par pays ──────────────────────────────────────
    public static class LigneLitige {
        private String bureau;
        private String codePays;
        private long nbContentieux;
        private long nbArbitrage;
        private long nbTotal;
        private Double pourcentage;

        public LigneLitige() {}

        public LigneLitige(String bureau, String codePays, long nbContentieux, long nbArbitrage) {
            this.bureau = bureau;
            this.codePays = codePays;
            this.nbContentieux = nbContentieux;
            this.nbArbitrage = nbArbitrage;
            this.nbTotal = nbContentieux + nbArbitrage;
        }

        public void setPourcentage(long grandTotal) {
            this.pourcentage = grandTotal == 0 ? 0.0
                    : BigDecimal.valueOf((double) nbTotal / grandTotal * 100)
                            .setScale(1, RoundingMode.HALF_UP).doubleValue();
        }

        public String getBureau() { return bureau; }
        public String getCodePays() { return codePays; }
        public long getNbContentieux() { return nbContentieux; }
        public long getNbArbitrage() { return nbArbitrage; }
        public long getNbTotal() { return nbTotal; }
        public Double getPourcentage() { return pourcentage; }
    }

    // ── Dossier individuel en litige ───────────────────────────────────────
    public static class DossierLitige {
        private String numero;
        private String dateDeclaration;
        private String assure;
        private String typeSinistre;
        private String paysPartenaire;
        private String statut;
        private String niveauJuridiction;
        private String dateProchaineAudience;

        public DossierLitige() {}

        public DossierLitige(String numero, String dateDeclaration, String assure,
                String typeSinistre, String paysPartenaire, String statut,
                String niveauJuridiction, String dateProchaineAudience) {
            this.numero = numero;
            this.dateDeclaration = dateDeclaration;
            this.assure = assure;
            this.typeSinistre = typeSinistre;
            this.paysPartenaire = paysPartenaire;
            this.statut = statut;
            this.niveauJuridiction = niveauJuridiction;
            this.dateProchaineAudience = dateProchaineAudience;
        }

        public String getNumero() { return numero; }
        public String getDateDeclaration() { return dateDeclaration; }
        public String getAssure() { return assure; }
        public String getTypeSinistre() { return typeSinistre; }
        public String getPaysPartenaire() { return paysPartenaire; }
        public String getStatut() { return statut; }
        public String getNiveauJuridiction() { return niveauJuridiction; }
        public String getDateProchaineAudience() { return dateProchaineAudience; }
    }
}
