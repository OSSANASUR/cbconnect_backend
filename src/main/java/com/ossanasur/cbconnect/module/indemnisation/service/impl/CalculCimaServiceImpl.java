package com.ossanasur.cbconnect.module.indemnisation.service.impl;

import com.ossanasur.cbconnect.common.enums.TypeSinistre;
import com.ossanasur.cbconnect.common.enums.TypeTable;
import com.ossanasur.cbconnect.common.enums.LienParente;
import com.ossanasur.cbconnect.module.baremes.repository.BaremeCapitalisationRepository;
import com.ossanasur.cbconnect.module.baremes.repository.BaremeValeurPointIpRepository;
import com.ossanasur.cbconnect.module.expertise.repository.ExpertiseMedicaleRepository;
import com.ossanasur.cbconnect.module.indemnisation.entity.AyantDroit;
import com.ossanasur.cbconnect.module.indemnisation.entity.OffreIndemnisation;
import com.ossanasur.cbconnect.module.indemnisation.repository.AyantDroitRepository;
import com.ossanasur.cbconnect.module.indemnisation.repository.OffreIndemnisationRepository;
import com.ossanasur.cbconnect.module.indemnisation.service.CalculCimaService;
import com.ossanasur.cbconnect.module.reclamation.repository.DossierReclamationRepository;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import com.ossanasur.cbconnect.module.sinistre.entity.Victime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;

/**
 * Implémentation des formules de calcul CIMA art. 258-266.
 * Toutes les formules sont documentées avec leur référence légale.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CalculCimaServiceImpl implements CalculCimaService {

    private final OffreIndemnisationRepository offreRepository;
    private final ExpertiseMedicaleRepository expertiseMedicaleRepository;
    private final DossierReclamationRepository dossierReclamationRepository;
    private final BaremeCapitalisationRepository baremeCapitalisationRepository;
    private final BaremeValeurPointIpRepository baremeValeurPointIpRepository;
    private final AyantDroitRepository ayantDroitRepository;

    private static final BigDecimal PCT_FRAIS_GESTION = new BigDecimal("0.05");
    private static final BigDecimal PCT_PENALITE_RETARD = new BigDecimal("0.05"); // 5% par mois
    private static final int DELAI_MAX_OFFRE_BLESSE_MOIS = 12;
    private static final int DELAI_MAX_OFFRE_DECES_MOIS = 8;

    @Override
    @Transactional
    public OffreIndemnisation calculerOffreBlesse(Victime victime, String loginAuteur) {
        Sinistre sinistre = victime.getSinistre();

        // Determination du SMIG retenu : MAX(SMIG pays gestionnaire, SMIG pays
        // résidence)
        BigDecimal smigGestionnaire = sinistre.getPaysGestionnaire() != null
                ? sinistre.getPaysGestionnaire().getSmigMensuel()
                : BigDecimal.ZERO;
        BigDecimal smigResidence = victime.getPaysResidence() != null
                ? victime.getPaysResidence().getSmigMensuel()
                : BigDecimal.ZERO;
        BigDecimal smigRetenu = smigGestionnaire.max(smigResidence);
        BigDecimal smigAnnuel = smigRetenu.multiply(new BigDecimal("12"));

        // Age de la victime à la consolidation
        LocalDate dateRef = LocalDate.now(); // sera la date de consolidation si disponible
        var expertise = expertiseMedicaleRepository.findByVictime(victime.getVictimeTrackingId())
                .stream().findFirst();
        if (expertise.isPresent() && expertise.get().getDateConsolidation() != null) {
            dateRef = expertise.get().getDateConsolidation();
        }
        int ageConsolidation = Period.between(victime.getDateNaissance(), dateRef).getYears();

        // Données expertise médicale
        BigDecimal tauxIpp = BigDecimal.ZERO;
        int dureeIttJours = 0;
        BigDecimal pctPretiumDoloris = BigDecimal.ZERO;
        BigDecimal pctPrejEsthetique = BigDecimal.ZERO;
        boolean tiercePersonne = false;

        if (expertise.isPresent()) {
            var exp = expertise.get();
            tauxIpp = exp.getTauxIpp() != null ? exp.getTauxIpp() : BigDecimal.ZERO;
            dureeIttJours = exp.getDureeIttJours() != null ? exp.getDureeIttJours() : 0;
            pctPretiumDoloris = exp.getPretiumDoloris() != null
                    ? new BigDecimal(exp.getPretiumDoloris().getPointsPct()).divide(new BigDecimal("100"))
                    : BigDecimal.ZERO;
            pctPrejEsthetique = exp.getPrejudiceEsthetique() != null
                    ? new BigDecimal(exp.getPrejudiceEsthetique().getPointsPct()).divide(new BigDecimal("100"))
                    : BigDecimal.ZERO;
            tiercePersonne = exp.isNecessiteTiercePersonne();
        }

        BigDecimal revenuMensuel = victime.getRevenuMensuel().compareTo(BigDecimal.ZERO) > 0
                ? victime.getRevenuMensuel()
                : smigRetenu;
        BigDecimal revenuAnnuel = revenuMensuel.multiply(new BigDecimal("12"));

        // Art. 258 – Frais médicaux
        BigDecimal fraisMedicaux = dossierReclamationRepository
                .findMontantRetenuByVictime(victime.getVictimeTrackingId())
                .orElse(BigDecimal.ZERO);

        // Art. 259 – ITT (conditionnel : perte de revenu prouvée)
        BigDecimal itt = BigDecimal.ZERO;
        if (dureeIttJours > 0 && revenuMensuel.compareTo(smigRetenu) > 0) {
            BigDecimal baseItt = revenuMensuel.multiply(BigDecimal.valueOf(dureeIttJours))
                    .divide(new BigDecimal("30"), 2, RoundingMode.HALF_UP);
            BigDecimal plafondItt = smigAnnuel.multiply(new BigDecimal("6"));
            itt = baseItt.min(plafondItt);
        }

        // Art. 260-a – Préjudice physiologique
        BigDecimal prejPhysio = BigDecimal.ZERO;
        if (tauxIpp.compareTo(BigDecimal.ZERO) > 0) {
            var baremeIP = baremeValeurPointIpRepository.findByAgeAndIpp(ageConsolidation, tauxIpp);
            if (baremeIP.isPresent()) {
                BigDecimal valeurPoint = revenuAnnuel.multiply(new BigDecimal(baremeIP.get().getValeurPoint()))
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                prejPhysio = valeurPoint.multiply(tauxIpp).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            }
        }

        // Art. 260-b – Préjudice économique (IPP >= 50%)
        BigDecimal prejEco = BigDecimal.ZERO;
        if (tauxIpp.compareTo(new BigDecimal("50")) >= 0) {
            BigDecimal perteMensuelle = revenuMensuel
                    .subtract(revenuMensuel.multiply(tauxIpp.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)));
            String tableCapitali = "M" + (ageConsolidation <= 24 ? "25" : "100");
            if ("F".equalsIgnoreCase(victime.getSexe()))
                tableCapitali = "F" + (ageConsolidation <= 24 ? "25" : "100");
            var bareme = baremeCapitalisationRepository.findByTypeAndAgeClose(tableCapitali, ageConsolidation);
            BigDecimal prixRente = bareme.map(b -> b.getPrixFrancRente()).orElse(BigDecimal.ONE);
            BigDecimal plafondPE = smigAnnuel.multiply(new BigDecimal("10"));
            prejEco = perteMensuelle.multiply(new BigDecimal("12")).multiply(prixRente).min(plafondPE);
        }

        // Art. 260-c – Préjudice moral blessé (IPP >= 80%)
        BigDecimal prejMoral = BigDecimal.ZERO;
        if (tauxIpp.compareTo(new BigDecimal("80")) >= 0) {
            prejMoral = smigAnnuel.multiply(new BigDecimal("2"));
        }

        // Art. 261 – Tierce personne (IPP >= 80% + prescription médicale)
        BigDecimal tiercePersonneAmt = BigDecimal.ZERO;
        if (tauxIpp.compareTo(new BigDecimal("80")) >= 0 && tiercePersonne) {
            BigDecimal ipTotal = prejPhysio.add(prejEco).add(prejMoral);
            tiercePersonneAmt = ipTotal.multiply(new BigDecimal("0.5"));
        }

        // Art. 262 – Pretium doloris + Préjudice esthétique
        BigDecimal pretiumDoloris = smigAnnuel.multiply(pctPretiumDoloris);
        BigDecimal prejEsthetique = smigAnnuel.multiply(pctPrejEsthetique);

        // Art. 263 – Préjudice de carrière (actif professionnel)
        BigDecimal prejCarriere = BigDecimal.ZERO;
        if ("ACTIF".equals(victime.getStatutActivite() != null ? victime.getStatutActivite().name() : "")) {
            BigDecimal base263 = revenuMensuel.multiply(new BigDecimal("6"));
            BigDecimal plafond263 = smigAnnuel.multiply(new BigDecimal("36"));
            prejCarriere = base263.min(plafond263);
        }

        // Art. 263-1 – Préjudice scolaire
        BigDecimal prejScolaire = BigDecimal.ZERO;
        if ("ELEVE_ETUDIANT".equals(victime.getStatutActivite() != null ? victime.getStatutActivite().name() : "")) {
            prejScolaire = revenuMensuel.multiply(new BigDecimal("12"));
        }

        // Art. 229 – Préjudice des lésés (IPP >= 80%)
        BigDecimal prejLeses = BigDecimal.ZERO;
        if (tauxIpp.compareTo(new BigDecimal("80")) >= 0) {
            prejLeses = smigAnnuel.multiply(new BigDecimal("2"));
        }

        // Total brut
        BigDecimal totalBrut = fraisMedicaux.add(itt).add(prejPhysio).add(prejEco)
                .add(prejMoral).add(tiercePersonneAmt).add(pretiumDoloris)
                .add(prejEsthetique).add(prejCarriere).add(prejScolaire).add(prejLeses);

        // Application du taux RC
        BigDecimal tauxRc = sinistre.getTauxRc() != null ? sinistre.getTauxRc() : new BigDecimal("100");
        BigDecimal totalNet = totalBrut.multiply(tauxRc).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        // Frais de gestion (5% si SURVENU_TOGO uniquement)
        BigDecimal fraisGestion = BigDecimal.ZERO;
        if (TypeSinistre.SURVENU_TOGO.equals(sinistre.getTypeSinistre())) {
            fraisGestion = totalNet.multiply(PCT_FRAIS_GESTION).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal montantTotalOffre = totalNet.add(fraisGestion);

        OffreIndemnisation offre = OffreIndemnisation.builder()
                .offreTrackingId(UUID.randomUUID())
                .smigMensuelRetenu(smigRetenu)
                .montantFraisMedicaux(fraisMedicaux).montantItt(itt)
                .montantPrejPhysiologique(prejPhysio).montantPrejEconomique(prejEco)
                .montantPrejMoral(prejMoral).montantTiercePersonne(tiercePersonneAmt)
                .montantPretiumDoloris(pretiumDoloris).montantPrejEsthetique(prejEsthetique)
                .montantPrejCarriere(prejCarriere).montantPrejScolaire(prejScolaire)
                .montantPrejLeses(prejLeses).montantFraisFuneraires(BigDecimal.ZERO)
                .totalBrut(totalBrut).tauxPartageRc(tauxRc)
                .totalNet(totalNet).fraisGestion(fraisGestion).montantTotalOffre(montantTotalOffre)
                .victime(victime).createdBy(loginAuteur).activeData(true).deletedData(false)
                .fromTable(TypeTable.OFFRE_INDEMNISATION)
                .build();

        log.info("Calcul CIMA blesse {} : montant total = {} FCFA", victime.getVictimeTrackingId(), montantTotalOffre);
        return offreRepository.save(offre);
    }

    @Override
    @Transactional
    public OffreIndemnisation calculerOffreDeces(Victime victime, String loginAuteur) {
        Sinistre sinistre = victime.getSinistre();

        // ── SMIG retenu ───────────────────────────────────────────────────────
        BigDecimal smigGestionnaire = sinistre.getPaysGestionnaire() != null
                ? sinistre.getPaysGestionnaire().getSmigMensuel()
                : BigDecimal.ZERO;
        BigDecimal smigResidence = victime.getPaysResidence() != null
                ? victime.getPaysResidence().getSmigMensuel()
                : BigDecimal.ZERO;
        BigDecimal smigRetenu = smigGestionnaire.max(smigResidence);
        BigDecimal smigAnnuel = smigRetenu.multiply(new BigDecimal("12"));

        BigDecimal revenuMensuel = victime.getRevenuMensuel().compareTo(BigDecimal.ZERO) > 0
                ? victime.getRevenuMensuel()
                : smigRetenu;
        BigDecimal revenuAnnuel = revenuMensuel.multiply(new BigDecimal("12"));

        // ── Art. 264 – Frais funéraires : 3 × SMIG mensuel retenu ────────────
        BigDecimal fraisFuneraires = smigRetenu.multiply(new BigDecimal("3"));

        // ── Art. 265 – Préjudice économique des ayants droit ──────────────────
        List<AyantDroit> ayantsDroit = ayantDroitRepository.findByVictime(victime.getVictimeTrackingId());

        long nbConjoints = ayantsDroit.stream()
                .filter(a -> a.getLien() == LienParente.CONJOINT || a.getLien() == LienParente.CONCUBINE).count();
        long nbEnfants = ayantsDroit.stream().filter(a -> a.getLien() == LienParente.ENFANT).count();
        long nbAscendants = ayantsDroit.stream()
                .filter(a -> a.getLien() == LienParente.PERE || a.getLien() == LienParente.MERE).count();

        // Clé de répartition art. 265 (tableau CIMA)
        BigDecimal cleConjoint, cleEnfant, cleAscendant;
        if (nbConjoints > 0 && nbEnfants > 0) {
            cleAscendant = new BigDecimal("0.05");
            if (nbEnfants <= 4) {
                cleConjoint = new BigDecimal("0.40");
                cleEnfant = new BigDecimal("0.30");
            } else {
                cleConjoint = new BigDecimal("0.35");
                cleEnfant = new BigDecimal("0.40");
            }
        } else if (nbConjoints > 0) { // conjoint(s) sans enfant
            cleAscendant = new BigDecimal("0.15");
            cleConjoint = new BigDecimal("0.40");
            cleEnfant = BigDecimal.ZERO;
        } else if (nbEnfants > 0) { // enfants sans conjoint
            cleAscendant = new BigDecimal("0.15");
            cleConjoint = BigDecimal.ZERO;
            cleEnfant = new BigDecimal("0.50");
        } else { // ascendants seulement
            cleAscendant = new BigDecimal("0.25");
            cleConjoint = BigDecimal.ZERO;
            cleEnfant = BigDecimal.ZERO;
        }

        BigDecimal plafondPE = smigAnnuel.multiply(new BigDecimal("85"));
        BigDecimal totalPeCalcule = BigDecimal.ZERO;

        // Calcul PE par bénéficiaire
        for (AyantDroit ad : ayantsDroit) {
            BigDecimal pe = BigDecimal.ZERO;
            int ageAd = Period.between(ad.getDateNaissance(), LocalDate.now()).getYears();
            boolean isEnfantMineur = ageAd < 21 || (ageAd < 25 && ad.isPoursuiteEtudes());

            if ((ad.getLien() == LienParente.CONJOINT || ad.getLien() == LienParente.CONCUBINE) && nbConjoints > 0) {
                BigDecimal revenuACap = revenuAnnuel.multiply(cleConjoint)
                        .divide(BigDecimal.valueOf(nbConjoints), 2, RoundingMode.HALF_UP);
                String table = "F".equalsIgnoreCase(ad.getSexe()) ? "F100" : "M100";
                BigDecimal prixRente = baremeCapitalisationRepository.findByTypeAndAgeClose(table, ageAd)
                        .map(b -> b.getPrixFrancRente()).orElse(BigDecimal.ONE);
                pe = revenuACap.multiply(prixRente);
            } else if (ad.getLien() == LienParente.ENFANT && isEnfantMineur && nbEnfants > 0) {
                // clé orphelin double si applicable
                BigDecimal cleOrphelin = ad.isEstOrphelinDouble()
                        ? (nbConjoints > 0 ? new BigDecimal("0.50") : new BigDecimal("0.60"))
                        : cleEnfant;
                BigDecimal revenuACap = revenuAnnuel.multiply(cleOrphelin)
                        .divide(BigDecimal.valueOf(nbEnfants), 2, RoundingMode.HALF_UP);
                String table = "F".equalsIgnoreCase(ad.getSexe()) ? "F25" : "M25";
                BigDecimal prixRente = baremeCapitalisationRepository.findByTypeAndAgeClose(table, ageAd)
                        .map(b -> b.getPrixFrancRente()).orElse(BigDecimal.ONE);
                pe = revenuACap.multiply(prixRente);
            } else if ((ad.getLien() == LienParente.PERE || ad.getLien() == LienParente.MERE) && nbAscendants > 0) {
                BigDecimal revenuACap = revenuAnnuel.multiply(cleAscendant)
                        .divide(BigDecimal.valueOf(nbAscendants), 2, RoundingMode.HALF_UP);
                String table = "F".equalsIgnoreCase(ad.getSexe()) ? "F100" : "M100";
                BigDecimal prixRente = baremeCapitalisationRepository.findByTypeAndAgeClose(table, ageAd)
                        .map(b -> b.getPrixFrancRente()).orElse(BigDecimal.ONE);
                pe = revenuACap.multiply(prixRente);
            }
            ad.setMontantPe(pe.setScale(2, RoundingMode.HALF_UP));
            totalPeCalcule = totalPeCalcule.add(pe);
        }

        // Application du plafond PE (art. 265 al. 3)
        BigDecimal totalPeFinal = totalPeCalcule;
        if (totalPeCalcule.compareTo(plafondPE) > 0) {
            totalPeFinal = plafondPE;
            final BigDecimal totBrut = totalPeCalcule;
            ayantsDroit.forEach(ad -> {
                BigDecimal peCorrige = plafondPE.multiply(ad.getMontantPe())
                        .divide(totBrut, 2, RoundingMode.HALF_UP);
                ad.setMontantPe(peCorrige);
            });
        }

        // ── Art. 266 – Préjudice moral des ayants droit ───────────────────────
        BigDecimal plafondPmVeuves = smigAnnuel.multiply(new BigDecimal("6"));
        BigDecimal plafondPmGlobal = smigAnnuel.multiply(new BigDecimal("20"));
        BigDecimal totalPmVeuves = BigDecimal.ZERO;
        BigDecimal totalPmGlobal = BigDecimal.ZERO;

        for (AyantDroit ad : ayantsDroit) {
            BigDecimal cle;
            switch (ad.getLien()) {
                case PERE, MERE -> cle = new BigDecimal("0.75");
                case CONJOINT, CONCUBINE -> cle = new BigDecimal("1.50");
                case ENFANT -> {
                    int ageAd = Period.between(ad.getDateNaissance(), LocalDate.now()).getYears();
                    cle = ageAd < 18 ? new BigDecimal("1.00") : new BigDecimal("0.75");
                }
                case FRERE, SOEUR -> cle = new BigDecimal("0.50");
                default -> cle = BigDecimal.ZERO;
            }
            BigDecimal pm = smigAnnuel.multiply(cle);
            ad.setMontantPm(pm.setScale(2, RoundingMode.HALF_UP));
            if (ad.getLien() == LienParente.CONJOINT || ad.getLien() == LienParente.CONCUBINE) {
                totalPmVeuves = totalPmVeuves.add(pm);
            }
            totalPmGlobal = totalPmGlobal.add(pm);
        }

        // Plafond veuves
        if (totalPmVeuves.compareTo(plafondPmVeuves) > 0) {
            final BigDecimal totV = totalPmVeuves;
            ayantsDroit.stream()
                    .filter(a -> a.getLien() == LienParente.CONJOINT || a.getLien() == LienParente.CONCUBINE)
                    .forEach(a -> a.setMontantPm(
                            plafondPmVeuves.multiply(a.getMontantPm()).divide(totV, 2, RoundingMode.HALF_UP)));
            totalPmGlobal = totalPmGlobal.subtract(totalPmVeuves).add(plafondPmVeuves);
        }

        // Plafond global PM
        if (totalPmGlobal.compareTo(plafondPmGlobal) > 0) {
            final BigDecimal totG = totalPmGlobal;
            ayantsDroit.forEach(a -> a.setMontantPm(
                    plafondPmGlobal.multiply(a.getMontantPm()).divide(totG, 2, RoundingMode.HALF_UP)));
            totalPmGlobal = plafondPmGlobal;
        }

        // Montant total par ayant droit
        ayantsDroit.forEach(a -> a.setMontantTotal(
                a.getMontantPe().add(a.getMontantPm()).setScale(2, RoundingMode.HALF_UP)));
        ayantDroitRepository.saveAll(ayantsDroit);

        // Totaux
        BigDecimal totalBrut = fraisFuneraires.add(totalPeFinal).add(totalPmGlobal);
        BigDecimal tauxRc = sinistre.getTauxRc() != null ? sinistre.getTauxRc() : new BigDecimal("100");
        BigDecimal totalNet = totalBrut.multiply(tauxRc).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal fraisGestion = BigDecimal.ZERO;
        if (TypeSinistre.SURVENU_TOGO.equals(sinistre.getTypeSinistre())) {
            fraisGestion = totalNet.multiply(PCT_FRAIS_GESTION).setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal montantTotalOffre = totalNet.add(fraisGestion);

        OffreIndemnisation offre = OffreIndemnisation.builder()
                .offreTrackingId(UUID.randomUUID())
                .smigMensuelRetenu(smigRetenu)
                .montantFraisFuneraires(fraisFuneraires)
                .montantPrejEconomique(totalPeFinal)
                .montantPrejMoral(totalPmGlobal)
                .montantFraisMedicaux(BigDecimal.ZERO).montantItt(BigDecimal.ZERO)
                .montantPrejPhysiologique(BigDecimal.ZERO).montantTiercePersonne(BigDecimal.ZERO)
                .montantPretiumDoloris(BigDecimal.ZERO).montantPrejEsthetique(BigDecimal.ZERO)
                .montantPrejCarriere(BigDecimal.ZERO).montantPrejScolaire(BigDecimal.ZERO)
                .montantPrejLeses(BigDecimal.ZERO)
                .totalBrut(totalBrut).tauxPartageRc(tauxRc)
                .totalNet(totalNet).fraisGestion(fraisGestion).montantTotalOffre(montantTotalOffre)
                .victime(victime).createdBy(loginAuteur).activeData(true).deletedData(false)
                .fromTable(TypeTable.OFFRE_INDEMNISATION)
                .build();

        log.info("Calcul CIMA deces {} ({} ayants droit) : montant total = {} FCFA",
                victime.getVictimeTrackingId(), ayantsDroit.size(), montantTotalOffre);
        return offreRepository.save(offre);
    }

    @Override
    public BigDecimal calculerPenalitesRetard(OffreIndemnisation offre) {
        LocalDate dateDeclaration = offre.getVictime().getSinistre().getDateDeclaration();
        long moisEcoules = java.time.temporal.ChronoUnit.MONTHS.between(dateDeclaration, LocalDate.now());
        boolean estDeces = com.ossanasur.cbconnect.common.enums.TypeVictime.DECEDE
                .equals(offre.getVictime().getTypeVictime());
        long delaiMax = estDeces ? DELAI_MAX_OFFRE_DECES_MOIS : DELAI_MAX_OFFRE_BLESSE_MOIS;
        long moisRetard = Math.max(0, moisEcoules - delaiMax);
        if (moisRetard == 0)
            return BigDecimal.ZERO;
        return offre.getMontantTotalOffre()
                .multiply(PCT_PENALITE_RETARD)
                .multiply(BigDecimal.valueOf(moisRetard))
                .setScale(2, RoundingMode.HALF_UP);
    }
}