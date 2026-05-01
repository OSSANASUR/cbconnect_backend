package com.ossanasur.cbconnect.module.statistiques.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * R6 — Réclamations Togo envers les bureaux homologues (sinistres ET = SURVENU_TOGO).
 * Structure : pays émetteur → compagnie homologue → liste de dossiers + récap global.
 */
public class ReclamationTogoDto {

    private List<BlocPays> blocs;
    private List<LigneRecap> recap;
    private long totalDossiers;
    private BigDecimal totalMontantReclame;
    private BigDecimal totalMontantRetenu;

    public ReclamationTogoDto(List<BlocPays> blocs, List<LigneRecap> recap,
                               long totalDossiers, BigDecimal totalMontantReclame,
                               BigDecimal totalMontantRetenu) {
        this.blocs              = blocs;
        this.recap              = recap;
        this.totalDossiers      = totalDossiers;
        this.totalMontantReclame = totalMontantReclame;
        this.totalMontantRetenu  = totalMontantRetenu;
    }

    public List<BlocPays>   getBlocs()               { return blocs; }
    public List<LigneRecap> getRecap()               { return recap; }
    public long             getTotalDossiers()        { return totalDossiers; }
    public BigDecimal       getTotalMontantReclame()  { return totalMontantReclame; }
    public BigDecimal       getTotalMontantRetenu()   { return totalMontantRetenu; }

    // ── Bloc par pays ────────────────────────────────────────────────────────
    public static class BlocPays {
        private String           pays;
        private String           codePays;
        private List<BlocCompagnie> compagnies;

        public BlocPays(String pays, String codePays, List<BlocCompagnie> compagnies) {
            this.pays       = pays;
            this.codePays   = codePays;
            this.compagnies = compagnies;
        }

        public String              getPays()       { return pays; }
        public String              getCodePays()   { return codePays; }
        public List<BlocCompagnie> getCompagnies() { return compagnies; }
    }

    // ── Bloc par compagnie ───────────────────────────────────────────────────
    public static class BlocCompagnie {
        private String           compagnie;
        private long             nbDossiers;
        private BigDecimal       montantReclame;
        private BigDecimal       montantRetenu;
        private List<LigneDossier> dossiers;

        public BlocCompagnie(String compagnie, List<LigneDossier> dossiers) {
            this.compagnie      = compagnie;
            this.dossiers       = dossiers;
            this.nbDossiers     = dossiers.size();
            this.montantReclame = dossiers.stream()
                    .map(LigneDossier::getMontantReclame)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            this.montantRetenu  = dossiers.stream()
                    .map(LigneDossier::getMontantRetenu)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        public String              getCompagnie()      { return compagnie; }
        public long                getNbDossiers()     { return nbDossiers; }
        public BigDecimal          getMontantReclame() { return montantReclame; }
        public BigDecimal          getMontantRetenu()  { return montantRetenu; }
        public List<LigneDossier>  getDossiers()       { return dossiers; }
    }

    // ── Ligne de dossier ─────────────────────────────────────────────────────
    public static class LigneDossier {
        private int        numero;
        private String     numeroDossier;
        private String     dateAccident;
        private String     sinBncbTg;
        private String     sinBncbPartenaire;
        private String     sinAssureurPartenaire;
        private String     assure;
        private String     victime;
        private BigDecimal montantReclame;
        private BigDecimal montantRetenu;
        private String     statutReclamation;
        private String     observations;
        private String     assureurTg;

        public LigneDossier(int numero, String numeroDossier, String dateAccident,
                             String sinBncbTg, String sinBncbPartenaire, String sinAssureurPartenaire,
                             String assure, String victime,
                             BigDecimal montantReclame, BigDecimal montantRetenu,
                             String statutReclamation, String observations, String assureurTg) {
            this.numero                = numero;
            this.numeroDossier         = numeroDossier;
            this.dateAccident          = dateAccident;
            this.sinBncbTg             = sinBncbTg;
            this.sinBncbPartenaire     = sinBncbPartenaire;
            this.sinAssureurPartenaire = sinAssureurPartenaire;
            this.assure                = assure;
            this.victime               = victime;
            this.montantReclame        = montantReclame;
            this.montantRetenu         = montantRetenu;
            this.statutReclamation     = statutReclamation;
            this.observations          = observations;
            this.assureurTg            = assureurTg;
        }

        public void       setNumero(int n)              { this.numero = n; }
        public int        getNumero()                   { return numero; }
        public String     getNumeroDossier()            { return numeroDossier; }
        public String     getDateAccident()             { return dateAccident; }
        public String     getSinBncbTg()                { return sinBncbTg; }
        public String     getSinBncbPartenaire()        { return sinBncbPartenaire; }
        public String     getSinAssureurPartenaire()    { return sinAssureurPartenaire; }
        public String     getAssure()                   { return assure; }
        public String     getVictime()                  { return victime; }
        public BigDecimal getMontantReclame()           { return montantReclame; }
        public BigDecimal getMontantRetenu()            { return montantRetenu; }
        public String     getStatutReclamation()        { return statutReclamation; }
        public String     getObservations()             { return observations; }
        public String     getAssureurTg()               { return assureurTg; }
    }

    // ── Ligne récap ──────────────────────────────────────────────────────────
    public static class LigneRecap {
        private String     pays;
        private String     codePays;
        private String     compagnie;
        private long       nbDossiers;
        private BigDecimal montantReclame;
        private BigDecimal montantRetenu;
        private double     pctNb;
        private double     pctMontant;

        public LigneRecap(String pays, String codePays, String compagnie, long nbDossiers,
                           BigDecimal montantReclame, BigDecimal montantRetenu) {
            this.pays           = pays;
            this.codePays       = codePays;
            this.compagnie      = compagnie;
            this.nbDossiers     = nbDossiers;
            this.montantReclame = montantReclame;
            this.montantRetenu  = montantRetenu;
        }

        public void calcPct(long totalNb, BigDecimal totalMt) {
            this.pctNb      = totalNb == 0 ? 0 :
                    round((double) nbDossiers / totalNb * 100);
            this.pctMontant = totalMt.compareTo(BigDecimal.ZERO) == 0 ? 0 :
                    round(montantReclame.divide(totalMt, 6, RoundingMode.HALF_UP)
                                        .multiply(BigDecimal.valueOf(100))
                                        .doubleValue());
        }

        private static double round(double v) {
            return BigDecimal.valueOf(v).setScale(1, RoundingMode.HALF_UP).doubleValue();
        }

        public String     getPays()           { return pays; }
        public String     getCodePays()       { return codePays; }
        public String     getCompagnie()      { return compagnie; }
        public long       getNbDossiers()     { return nbDossiers; }
        public BigDecimal getMontantReclame() { return montantReclame; }
        public BigDecimal getMontantRetenu()  { return montantRetenu; }
        public double     getPctNb()          { return pctNb; }
        public double     getPctMontant()     { return pctMontant; }
    }
}
