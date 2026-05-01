package com.ossanasur.cbconnect.module.reprise.service;

import com.ossanasur.cbconnect.common.enums.StatutActivite;
import com.ossanasur.cbconnect.common.enums.StatutCheque;
import com.ossanasur.cbconnect.common.enums.StatutPaiement;
import com.ossanasur.cbconnect.common.enums.StatutSinistre;
import com.ossanasur.cbconnect.common.enums.TypeDommage;
import com.ossanasur.cbconnect.common.enums.TypeOperationFinanciere;
import com.ossanasur.cbconnect.common.enums.TypeOrganisme;
import com.ossanasur.cbconnect.common.enums.TypeSinistre;
import com.ossanasur.cbconnect.module.auth.entity.Organisme;
import com.ossanasur.cbconnect.module.auth.repository.OrganismeRepository;
import com.ossanasur.cbconnect.module.finance.entity.Encaissement;
import com.ossanasur.cbconnect.module.finance.entity.Paiement;
import com.ossanasur.cbconnect.module.finance.repository.EncaissementRepository;
import com.ossanasur.cbconnect.module.finance.repository.PaiementRepository;
import com.ossanasur.cbconnect.module.finance.service.NumeroOperationGenerator;
import com.ossanasur.cbconnect.module.pays.entity.Pays;
import com.ossanasur.cbconnect.module.pays.repository.PaysRepository;
import com.ossanasur.cbconnect.module.reprise.dto.RapportReprise;
import com.ossanasur.cbconnect.module.reprise.dto.RepriseOrganismesRequest;
import com.ossanasur.cbconnect.module.reprise.dto.RepriseSinistresRequest;
import com.ossanasur.cbconnect.module.reprise.dto.RapportReprise.DetailReprise;
import com.ossanasur.cbconnect.module.reprise.dto.RepriseEncaissementsRequest;
import com.ossanasur.cbconnect.module.reprise.dto.RepriseEncaissementsRequest.EncaissementRepriseDto;
import com.ossanasur.cbconnect.module.reprise.dto.RepriseOrganismesRequest.OrganismeRepriseDto;
import com.ossanasur.cbconnect.module.reprise.dto.ReprisePaiementsRequest;
import com.ossanasur.cbconnect.module.reprise.dto.ReprisePaiementsRequest.PaiementRepriseDto;
import com.ossanasur.cbconnect.module.reprise.dto.RepriseSinistresRequest.SinistreRepriseDto;
import com.ossanasur.cbconnect.module.sinistre.entity.Assure;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import com.ossanasur.cbconnect.module.sinistre.entity.Victime;
import com.ossanasur.cbconnect.module.sinistre.repository.AssureRepository;
import com.ossanasur.cbconnect.module.sinistre.repository.SinistreRepository;
import com.ossanasur.cbconnect.module.sinistre.repository.VictimeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Service de reprise des données historiques BNCB Togo 2025.
 *
 * Logique d'import sinistres :
 * • Détection doublon sur numeroSinistreManuel (index unique en base)
 * • Lookup pays par codeIso ou codeCarteBrune (BJ → Bénin)
 * • Lookup organisme membre par code assureur (ex: "SUNU BJ")
 * • Création d'un Assure (relation obligatoire sur Sinistre) à partir du nom
 * complet et de l'immatriculation issus du fichier Excel
 * • Statut initial = NOUVEAU
 * • Flag repriseHistorique = true (pour distinguer des saisies normales)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RepriseService {

    private final SinistreRepository sinistreRepository;
    private final OrganismeRepository organismeRepository;
    private final PaysRepository paysRepository;
    private final EncaissementRepository encaissementRepository;
    private final PaiementRepository paiementRepository;
    private final VictimeRepository victimeRepository;
    // Sinistre.assure est nullable=false → on doit persister un Assure avant le
    // sinistre.
    private final AssureRepository assureRepository;
    private final NumeroOperationGenerator numeroOperationGenerator;

    // Self-injection via proxy Spring : nécessaire pour que les @Transactional
    // REQUIRES_NEW des méthodes par-élément soient réellement interceptées
    // (un appel direct `this.importerUnSinistre(...)` contourne le proxy AOP).
    // @Lazy évite le cycle de dépendance à la construction du bean.
    @Lazy
    @Autowired
    private RepriseService self;

    // Cache en mémoire pour la session d'import (évite N requêtes SQL par sinistre)
    private final Map<String, Pays> cacheP = new HashMap<>();
    private final Map<String, Organisme> cacheO = new HashMap<>();
    // Cache bureaux homologues : codePays → Organisme BUREAU_HOMOLOGUE
    // Chargé une seule fois au premier appel (lazy init).
    private final Map<String, Organisme> cacheH = new HashMap<>();

    // ─── Import sinistres ────────────────────────────────────────────────────

    /**
     * Méthode d'orchestration : itère sur la liste et délègue chaque import à
     * `self.importerUnSinistre` qui tourne dans sa propre transaction
     * (REQUIRES_NEW). Résultat : une ligne fautive roll-back seule sans tuer
     * la batch. Pas de @Transactional ici — on ne veut PAS de tx englobante.
     */
    public RapportReprise importerSinistres(RepriseSinistresRequest req, String loginAuteur) {
        int importes = 0, doublons = 0, erreurs = 0;
        List<DetailReprise> details = new ArrayList<>();

        for (SinistreRepriseDto dto : req.sinistres()) {
            try {
                ResultatImport res = self.importerUnSinistre(dto, loginAuteur);
                if (res == ResultatImport.IMPORTE) {
                    importes++;
                } else { // DOUBLON
                    doublons++;
                    details.add(new DetailReprise("DOUBLON", dto.numeroSinistreManuel(),
                            "Déjà présent en base"));
                }
            } catch (Exception e) {
                erreurs++;
                log.warn("[REPRISE] Erreur import sinistre {} : {}", dto.numeroSinistreManuel(), e.getMessage());
                details.add(new DetailReprise("ERROR", dto.numeroSinistreManuel(), e.getMessage()));
            }
        }

        return new RapportReprise(req.sinistres().size(), importes, doublons, erreurs,
                details.isEmpty() ? List.of() : details);
    }

    /**
     * Import d'UN sinistre dans sa propre transaction (REQUIRES_NEW).
     * Doit impérativement être appelée via `self` pour que le proxy Spring
     * applique la propagation. Erreurs remontées en RuntimeException :
     * l'appelant (importerSinistres) les catch et les comptabilise.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResultatImport importerUnSinistre(SinistreRepriseDto dto, String loginAuteur) {
        // ── Doublon ──
        if (sinistreRepository.existsByNumeroSinistreManuel(dto.numeroSinistreManuel())) {
            return ResultatImport.DOUBLON;
        }

        // ── Validation date accident (NOT NULL en base) ──
        LocalDate dateAccident = parseDate(dto.dateAccident());
        if (dateAccident == null) {
            throw new IllegalArgumentException("Date accident absente ou illisible : " + dto.dateAccident());
        }

        // ── Type sinistre (requis) ──
        TypeSinistre type = TypeSinistre.valueOf(dto.typeSinistre());

        // ── Résolution pays (sémantique corrigée) ──
        // Rappel :
        //   ET (SURVENU_TOGO)     : gestionnaire = TG, émetteur = pays étranger
        //   TE (SURVENU_ETRANGER) : gestionnaire = pays étranger, émetteur = TG
        Pays paysTG = resoudrePays("TG");
        Pays paysEmetteur    = resoudrePays(dto.paysEmetteurCode());
        Pays paysGestionnaire = resoudrePays(dto.paysGestionnaireCode());

        // Fallback intelligent selon le type si un pays manque dans l'Excel
        if (paysGestionnaire == null) {
            paysGestionnaire = (type == TypeSinistre.SURVENU_TOGO) ? paysTG : paysEmetteur;
        }
        if (paysEmetteur == null) {
            paysEmetteur = (type == TypeSinistre.SURVENU_ETRANGER) ? paysTG : null;
        }

        // Validation cohérence : au moins un des deux pays doit être TG
        if (paysGestionnaire == null) {
            throw new IllegalArgumentException("Pays gestionnaire introuvable pour "
                    + dto.numeroSinistreManuel() + " (type=" + type + ")");
        }
        if (type == TypeSinistre.SURVENU_TOGO && !isTG(paysGestionnaire)) {
            log.warn("[REPRISE] Sinistre ET {} : paysGestionnaire attendu=TG, reçu={}",
                    dto.numeroSinistreManuel(), paysGestionnaire.getCodeIso());
        }
        if (type == TypeSinistre.SURVENU_ETRANGER && (paysEmetteur == null || !isTG(paysEmetteur))) {
            log.warn("[REPRISE] Sinistre TE {} : paysEmetteur attendu=TG, reçu={}",
                    dto.numeroSinistreManuel(), paysEmetteur != null ? paysEmetteur.getCodeIso() : "null");
        }

        // ── Résolution organisme membre (assureur déclarant) ──
        Organisme organismeMembre = resoudreOrganisme(dto.assureurDeclarant());

        // ── Résolution bureau homologue = BCB du pays étranger ──
        // (toujours celui qui n'est pas TG, quel que soit le type de sinistre)
        Organisme organismeHomologue = resoudreOrganismeHomologueEtranger(paysEmetteur, paysGestionnaire);

        // ── Création de l'Assure (obligatoire : relation @ManyToOne nullable=false) ──
        // Le fichier Excel ne fournit qu'un nom complet brut : on remplit
        // nomAssure et nomComplet avec la même valeur (les deux sont NOT NULL).
        Assure assure = creerAssure(dto, organismeMembre, loginAuteur);
        assureRepository.save(assure);

        // Fallback : si numeroSinistreLocal est absent du DTO, on réutilise
        // le numéro manuel pour respecter la contrainte NOT NULL/UNIQUE.
        String numLocal = dto.numeroSinistreLocal() != null && !dto.numeroSinistreLocal().isBlank()
                ? dto.numeroSinistreLocal()
                : dto.numeroSinistreManuel();

        // ── Construction entité Sinistre ──
        Sinistre sinistre = Sinistre.builder()
                .sinistreTrackingId(UUID.randomUUID())
                .numeroSinistreManuel(dto.numeroSinistreManuel())
                .numeroSinistreLocal(numLocal)
                .numeroSinistreEcarteBrune(clean(dto.numeroSinistreEcarteBrune()))
                .numeroSinistreHomologue(clean(dto.numeroSinistreHomologue()))
                .typeSinistre(type)
                .typeDommage(parseTypeDommage(dto.typeDommage()))
                // En reprise, date déclaration = date accident (le fichier BNCB
                // ne distingue pas les deux).
                .dateAccident(dateAccident)
                .dateDeclaration(dateAccident)
                .assure(assure)
                .assureurDeclarant(clean(dto.assureurDeclarant()))
                .numeroPoliceAssureur(clean(dto.numeroPoliceAssureur()))
                .paysEmetteur(paysEmetteur)
                .paysGestionnaire(paysGestionnaire)
                .organismeMembre(organismeMembre)
                .organismeHomologue(organismeHomologue)
                .statut(StatutSinistre.NOUVEAU)
                .repriseHistorique(true)
                .createdBy(loginAuteur)
                .activeData(true)
                .deletedData(false)
                .build();

        sinistreRepository.save(sinistre);
        return ResultatImport.IMPORTE;
    }

    // ─── Import encaissements ────────────────────────────────────────────────

    /**
     * Orchestration : per-item REQUIRES_NEW (même pattern que
     * sinistres/organismes).
     * Pas de @Transactional ici — une tx englobante empêcherait la reprise après
     * erreur (rollback-only cascade → "null identifier" à la ligne suivante).
     */
    public RapportReprise importerEncaissements(RepriseEncaissementsRequest req, String loginAuteur) {
        int importes = 0, doublons = 0, erreurs = 0;
        List<DetailReprise> details = new ArrayList<>();

        for (EncaissementRepriseDto dto : req.encaissements()) {
            try {
                ResultatImport res = self.importerUnEncaissement(dto, loginAuteur, details);
                if (res == ResultatImport.IMPORTE) {
                    importes++;
                } else {
                    doublons++;
                }
            } catch (Exception e) {
                erreurs++;
                log.warn("[REPRISE] Erreur import encaissement {} : {}", dto.numeroCheque(), e.getMessage());
                details.add(new DetailReprise("ERROR", dto.numeroCheque(), e.getMessage()));
            }
        }

        return new RapportReprise(req.encaissements().size(), importes, doublons, erreurs,
                details.isEmpty() ? List.of() : details);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResultatImport importerUnEncaissement(EncaissementRepriseDto dto,
            String loginAuteur,
            List<DetailReprise> details) {
        // ── 1. Doublon : même numeroCheque + même organismeEmetteur ──
        Organisme organismeEmetteur = resoudreOrganisme(dto.organismeEmetteurCode());
        if (organismeEmetteur == null) {
            throw new IllegalArgumentException(
                    "Organisme émetteur introuvable : " + dto.organismeEmetteurCode());
        }
        if (encaissementRepository.existsByNumeroChequeAndOrganismeEmetteur(
                dto.numeroCheque(), organismeEmetteur)) {
            details.add(new DetailReprise("DOUBLON", dto.numeroCheque(),
                    "Chèque déjà enregistré pour cet organisme"));
            return ResultatImport.DOUBLON;
        }

        // ── 2. Résolution du sinistre ──
        Sinistre sinistre = sinistreRepository
                .findByNumeroSinistreManuel(dto.numeroSinistreManuel())
                .orElse(null);

        if (sinistre == null) {
            sinistre = creerSinistreMinimal(dto, organismeEmetteur, loginAuteur);
            sinistreRepository.save(sinistre);
            details.add(new DetailReprise("SINISTRE_CREE", dto.numeroCheque(),
                    "Sinistre minimal créé : " + dto.numeroSinistreManuel()));
        }

        // ── 3. Calcul des montants ──
        BigDecimal montantTheorique = coalesce(dto.montantTheorique());
        BigDecimal pgEncaisse = coalesce(dto.pgEncaisse());
        BigDecimal produitFraisGestion = coalesce(dto.produitFraisGestion());
        BigDecimal montantCheque = montantTheorique.add(pgEncaisse);

        // dateEmission est NOT NULL en base. En reprise l'Excel ne fournit
        // qu'une date de réception → on l'utilise aussi comme émission.
        LocalDate dateReception = parseDate(dto.dateReception());
        if (dateReception == null) {
            throw new IllegalArgumentException(
                    "Date de réception absente ou illisible : " + dto.dateReception());
        }

        // ── 4. Construction encaissement ──
        Encaissement enc = Encaissement.builder()
                .encaissementTrackingId(UUID.randomUUID())
                .numeroCheque(clean(dto.numeroCheque()))
                .montantCheque(montantCheque)
                .montantTheorique(montantTheorique)
                .produitFraisGestion(produitFraisGestion)
                .dateEmission(dateReception)
                .dateReception(dateReception)
                .dateEncaissement(dateReception)
                .banqueEmettrice(clean(dto.banqueEmettrice()))
                .modePaiement(clean(dto.modePaiement()))
                .statutCheque(StatutCheque.RECU)
                .organismeEmetteur(organismeEmetteur)
                .sinistre(sinistre)
                .repriseHistorique(true)
                .createdBy(loginAuteur)
                .activeData(true)
                .deletedData(false)
                .build();

        encaissementRepository.save(enc);
        return ResultatImport.IMPORTE;
    }

    // ─── Import organismes ───────────────────────────────────────────────────

    /**
     * Orchestration : propagation per-item via REQUIRES_NEW (voir
     * importerSinistres).
     * Le "currentPays" (contexte hérité de la ligne d'en-tête Excel précédente)
     * est géré ici, hors transaction, puis passé en paramètre à chaque appel.
     */
    public RapportReprise importerOrganismes(RepriseOrganismesRequest req, String loginAuteur) {
        int importes = 0, doublons = 0, erreurs = 0;
        List<DetailReprise> details = new ArrayList<>();
        // Le fichier Excel groupe les organismes par pays via une ligne d'en-tête :
        // on mémorise le dernier pays vu pour l'appliquer aux lignes suivantes.
        String currentPays = null;

        for (OrganismeRepriseDto dto : req.organismes()) {
            if (dto.pays() != null) {
                currentPays = dto.pays();
            }
            try {
                ResultatImport res = self.importerUnOrganisme(dto, currentPays, loginAuteur);
                if (res == ResultatImport.IMPORTE) {
                    importes++;
                } else { // DOUBLON
                    doublons++;
                }
            } catch (Exception e) {
                erreurs++;
                log.warn("[REPRISE] Erreur import organisme {} : {}", dto.code(), e.getMessage());
                details.add(new DetailReprise("ERROR", dto.code(), e.getMessage()));
            }
        }

        return new RapportReprise(req.organismes().size(), importes, doublons, erreurs,
                details.isEmpty() ? List.of() : details);
    }

    /**
     * Import d'UN organisme dans sa propre transaction. Même logique de proxy
     * que importerUnSinistre.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResultatImport importerUnOrganisme(OrganismeRepriseDto dto, String currentPays, String loginAuteur) {
        // Doublon sur code
        if (organismeRepository.existsByCode(dto.code())) {
            return ResultatImport.DOUBLON;
        }

        // Résolution pays par libellé (ligne d'en-tête Excel)
        Pays pays = null;
        if (currentPays != null) {
            pays = paysRepository.findByLibelleContainingIgnoreCase(currentPays).orElse(null);
        }
        // Fallback : extraire code du code compagnie ("SUNU BJ" → "BJ")
        if (pays == null && dto.code() != null) {
            String[] parts = dto.code().trim().split(" ");
            if (parts.length >= 2) {
                String codePays = parts[parts.length - 1].toUpperCase();
                pays = resoudrePays(codePays);
            }
        }

        // Organisme.email est UNIQUE + NOT NULL mais la reprise Excel ne fournit
        // pas d'email → on génère un email synthétique basé sur le code (sera
        // complété manuellement après import).
        String emailSynthetique = "reprise-" +
                dto.code().trim().toLowerCase().replace(" ", "-") +
                "@cbconnect.local";

        Organisme org = Organisme.builder()
                .organismeTrackingId(UUID.randomUUID())
                .raisonSociale(dto.raisonSociale())
                .code(dto.code())
                .typeOrganisme(TypeOrganisme.COMPAGNIE_MEMBRE)
                // Organisme n'a pas de relation @ManyToOne vers Pays :
                // on alimente les trois champs de référence disponibles.
                .paysId(pays != null ? pays.getHistoriqueId() : null)
                .codePaysBCB(pays != null ? pays.getCodeCarteBrune() : null)
                .codePays(pays != null ? pays.getCodeIso() : null)
                .email(emailSynthetique)
                .active(true)
                .repriseHistorique(true)
                .createdBy(loginAuteur)
                .activeData(true)
                .deletedData(false)
                .build();

        organismeRepository.save(org);
        cacheO.put(dto.code().toUpperCase(), org);
        return ResultatImport.IMPORTE;
    }

    /** Résultat d'un import unitaire : soit importé, soit doublon ignoré. */
    private enum ResultatImport {
        IMPORTE, DOUBLON
    }

    // ─── Statut ──────────────────────────────────────────────────────────────

    public Map<String, Object> getStatut() {
        long totalSinistres = sinistreRepository.countByRepriseHistorique(true);
        long totalET = sinistreRepository.countByTypeSinistreAndRepriseHistorique(TypeSinistre.SURVENU_TOGO, true);
        long totalTE = sinistreRepository.countByTypeSinistreAndRepriseHistorique(TypeSinistre.SURVENU_ETRANGER, true);
        long totalOrg = organismeRepository.countByRepriseHistorique(true);
        return Map.of(
                "sinistreReprises", totalSinistres,
                "sinistreET", totalET,
                "sinistreTE", totalTE,
                "organismeImportes", totalOrg,
                "message", totalSinistres == 0
                        ? "Aucune reprise effectuée"
                        : totalSinistres + " sinistres importés via reprise");
    }

    // ─── Helpers privés ──────────────────────────────────────────────────────

    /**
     * Crée l'entité Assure à partir du DTO de reprise.
     * nomAssure et nomComplet sont NOT NULL en base : on utilise la même valeur
     * pour les deux car le fichier Excel BNCB ne fournit qu'un nom unique.
     */
    private Assure creerAssure(SinistreRepriseDto dto, Organisme organisme, String loginAuteur) {
        String nomComplet = clean(dto.assureNomComplet());
        // Garde-fou : si le nom est absent, on met un placeholder pour ne pas
        // violer la contrainte NOT NULL — l'utilisateur pourra corriger plus tard.
        if (nomComplet == null || nomComplet.isBlank()) {
            nomComplet = "INCONNU";
        }
        // Troncature défensive : les colonnes DB de assure ont des longueurs
        // strictes qui peuvent être dépassées par des valeurs Excel bruitées
        // (nom_assure VARCHAR(100), nom_complet VARCHAR(200), immatriculation
        // VARCHAR(30)).
        return Assure.builder()
                .assureTrackingId(UUID.randomUUID())
                .nomAssure(tronquer(nomComplet, 100))
                .nomComplet(tronquer(nomComplet, 200))
                .immatriculation(tronquer(clean(dto.assureImmatriculation()), 30))
                .organisme(organisme)
                .createdBy(loginAuteur)
                .activeData(true)
                .deletedData(false)
                .build();
    }

    /**
     * Crée un sinistre minimal à partir des données de l'encaissement.
     *
     * Sémantique pays/organismes (validée avec l'utilisateur) :
     *  - paysGestionnaire = pays OÙ L'ACCIDENT A EU LIEU
     *  - paysEmetteur     = pays qui A ÉMIS LA CARTE BRUNE de l'assuré
     *  - organismeHomologue = BCB ÉTRANGER (dans les 2 cas de figure)
     *
     * ET (SURVENU_TOGO — étranger accidenté au Togo) :
     *   paysGestionnaire = TG
     *   paysEmetteur     = pays étranger du véhicule (BJ, BF, CI…)
     *
     * TE (SURVENU_ETRANGER — Togolais accidenté à l'étranger) :
     *   paysGestionnaire = pays étranger où l'accident a eu lieu
     *   paysEmetteur     = TG
     */
    private Sinistre creerSinistreMinimal(EncaissementRepriseDto dto,
            Organisme organisme,
            String loginAuteur) {
        TypeSinistre type = "T".equalsIgnoreCase(dto.lieuSurvenance())
                ? TypeSinistre.SURVENU_TOGO
                : TypeSinistre.SURVENU_ETRANGER;

        // Le pays extrait du code organisme est TOUJOURS le pays étranger
        // dans la logique Carte Brune : l'organisme lié à un encaissement est
        // soit le BCB étranger qui rembourse TG (ET), soit l'assureur togolais
        // qui remonte les fonds vers TG (TE, cas rare).
        Pays paysTG = resoudrePays("TG");
        Pays paysOrg = extrairePaysDepuisOrganisme(dto.organismeEmetteurCode());

        Pays paysGestionnaire, paysEmetteur;
        if (type == TypeSinistre.SURVENU_TOGO) {
            paysGestionnaire = paysTG;
            paysEmetteur     = (paysOrg != null && !isTG(paysOrg)) ? paysOrg : null;
        } else {
            paysGestionnaire = (paysOrg != null && !isTG(paysOrg)) ? paysOrg : null;
            paysEmetteur     = paysTG;
        }

        // Fallback garde-fou : paysGestionnaire est NOT NULL en base
        if (paysGestionnaire == null) {
            paysGestionnaire = paysTG;
        }

        // Organisme homologue = BCB du pays étranger (qui n'est pas TG)
        Organisme homologue = resoudreOrganismeHomologueEtranger(paysEmetteur, paysGestionnaire);

        Assure assure = creerAssureMinimal(dto.assureNomComplet(), organisme, loginAuteur);
        assureRepository.save(assure);

        String numLocal = dto.numeroSinistreManuel();

        return Sinistre.builder()
                .sinistreTrackingId(UUID.randomUUID())
                .numeroSinistreManuel(dto.numeroSinistreManuel())
                .numeroSinistreLocal(numLocal)
                .numeroSinistreHomologue(clean(dto.numeroSinistreHomologue()))
                .typeSinistre(type)
                .typeDommage(TypeDommage.MATERIEL)
                .dateAccident(parseDate(dto.dateSinistre()))
                .dateDeclaration(parseDate(dto.dateSinistre()))
                .assure(assure)
                .paysGestionnaire(paysGestionnaire)
                .paysEmetteur(paysEmetteur)
                .organismeMembre(organisme)
                .organismeHomologue(homologue)
                .assureurDeclarant(clean(dto.organismeEmetteurCode()))
                .statut(StatutSinistre.NOUVEAU)
                .repriseHistorique(true)
                .createdBy(loginAuteur)
                .activeData(true)
                .deletedData(false)
                .build();
    }

    private boolean isTG(Pays p) {
        return p != null && ("TG".equalsIgnoreCase(p.getCodeIso()) || "TG".equalsIgnoreCase(p.getCodeCarteBrune()));
    }

    private Assure creerAssureMinimal(String nom, Organisme organisme, String loginAuteur) {
        String nomComplet = (nom != null && !nom.isBlank()) ? nom.trim() : "INCONNU";
        return Assure.builder()
                .assureTrackingId(UUID.randomUUID())
                .nomAssure(nomComplet)
                .nomComplet(nomComplet)
                .organisme(organisme)
                .createdBy(loginAuteur)
                .activeData(true)
                .deletedData(false)
                .build();
    }

    /**
     * Extrait le pays depuis le code organisme payeur.
     * Ex: "BNCB BF" → cherche "BF" | "FIDELIA TG" → "TG" | "SANLAM TG" → "TG"
     */
    private Pays extrairePaysDepuisOrganisme(String codeOrganisme) {
        if (codeOrganisme == null || codeOrganisme.isBlank())
            return resoudrePays("TG");
        String[] parts = codeOrganisme.trim().split("\\s+");
        if (parts.length >= 2) {
            String codePays = parts[parts.length - 1].toUpperCase();
            Pays p = resoudrePays(codePays);
            if (p != null)
                return p;
        }
        return resoudrePays("TG");
    }

    private static BigDecimal coalesce(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    /** Tronque une chaîne à la longueur max (null-safe). */
    private String tronquer(String s, int max) {
        if (s == null)
            return null;
        return s.length() <= max ? s : s.substring(0, max);
    }

    /**
     * Résout un pays par son code ISO 2 lettres (BJ, BF, TG…) ou code Carte Brune.
     * Utilise un cache pour éviter des requêtes répétées.
     */
    private Pays resoudrePays(String code) {
        if (code == null || code.isBlank())
            return null;
        String key = code.trim().toUpperCase();
        if (cacheP.containsKey(key))
            return cacheP.get(key);
        Pays p = paysRepository.findByCodeCarteBrune(key)
                .or(() -> paysRepository.findByCodeIso(key))
                .orElse(null);
        if (p != null)
            cacheP.put(key, p);
        return p;
    }

    /**
     * Résout le bureau homologue (BUREAU_HOMOLOGUE) à partir du code pays émetteur.
     * Ex : "BJ" → BCB-BJ (Bureau National Carte Brune du Bénin).
     *
     * Charge tous les BUREAU_HOMOLOGUE en mémoire au premier appel (14 entrées max)
     * pour éviter une requête SQL par sinistre lors des 1350 imports.
     */
    /**
     * Résout l'organisme BCB du pays étranger à partir des pays gestionnaire
     * et émetteur d'un sinistre. L'homologue est toujours le BCB qui n'est PAS
     * celui du Togo, quelle que soit l'orientation ET ou TE du sinistre.
     */
    private Organisme resoudreOrganismeHomologueEtranger(Pays paysEmetteur, Pays paysGestionnaire) {
        Pays etranger = (paysEmetteur != null && !isTG(paysEmetteur)) ? paysEmetteur
                      : (paysGestionnaire != null && !isTG(paysGestionnaire)) ? paysGestionnaire
                      : null;
        if (etranger == null) return null;
        String code = etranger.getCodeCarteBrune() != null ? etranger.getCodeCarteBrune() : etranger.getCodeIso();
        return resoudreOrganismeHomologue(code);
    }

    private Organisme resoudreOrganismeHomologue(String codePays) {
        if (codePays == null || codePays.isBlank())
            return null;
        // Initialisation lazy du cache au premier appel
        if (cacheH.isEmpty()) {
            organismeRepository.findAllActiveByType(TypeOrganisme.BUREAU_HOMOLOGUE)
                    .forEach(o -> {
                        if (o.getCodePaysBCB() != null)
                            cacheH.put(o.getCodePaysBCB().toUpperCase(), o);
                        if (o.getCodePays() != null)
                            cacheH.put(o.getCodePays().toUpperCase(), o);
                    });
        }
        return cacheH.get(codePays.trim().toUpperCase());
    }

    /**
     * Ex : "SUNU BJ" → cherche code="SUNU BJ" ou raisonSociale contenant "SUNU BJ".
     *
     * Fallback reprise : l'Excel encaissements utilise "BNCB XX" pour désigner
     * les bureaux homologues, alors que la base les enregistre en "BCB-XX"
     * (cf V11). Si la recherche directe échoue et que l'entrée matche
     * "BNCB XX", on retente avec "BCB-XX".
     */
    private Organisme resoudreOrganisme(String assureurDeclarant) {
        if (assureurDeclarant == null || assureurDeclarant.isBlank())
            return null;
        String raw = assureurDeclarant.trim();
        String key = raw.toUpperCase();
        if (cacheO.containsKey(key))
            return cacheO.get(key);
        Organisme o = organismeRepository.findByCodeIgnoreCase(raw)
                .or(() -> organismeRepository.findFirstByRaisonSocialeContainingIgnoreCase(raw))
                .orElse(null);
        if (o == null) {
            String alt = normaliserCodeBureauHomologue(raw);
            if (alt != null) {
                o = organismeRepository.findByCodeIgnoreCase(alt).orElse(null);
            }
        }
        if (o != null)
            cacheO.put(key, o);
        return o;
    }

    /**
     * Orchestration : per-item REQUIRES_NEW (même pattern que
     * sinistres/encaissements/organismes). Une tx englobante provoquerait
     * "Transaction silently rolled back" dès la première ligne fautive.
     */
    public RapportReprise importerPaiements(ReprisePaiementsRequest req, String loginAuteur) {
        int importes = 0, doublons = 0, erreurs = 0;
        List<DetailReprise> details = new ArrayList<>();

        for (PaiementRepriseDto dto : req.paiements()) {
            try {
                ResultatImport res = self.importerUnPaiement(dto, loginAuteur, details);
                if (res == ResultatImport.IMPORTE) {
                    importes++;
                } else {
                    doublons++;
                }
            } catch (Exception e) {
                erreurs++;
                log.warn("[REPRISE] Erreur import paiement {} : {}", dto.numeroChequeEmis(), e.getMessage());
                details.add(new DetailReprise("ERROR", dto.numeroChequeEmis(), e.getMessage()));
            }
        }

        return new RapportReprise(req.paiements().size(), importes, doublons, erreurs,
                details.isEmpty() ? List.of() : details);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResultatImport importerUnPaiement(PaiementRepriseDto dto,
            String loginAuteur,
            List<DetailReprise> details) {
        // ── 1. Doublon : même numeroChequeEmis ──────────────
        if (paiementRepository.existsByNumeroChequeEmisAndActiveDataTrueAndDeletedDataFalse(
                clean(dto.numeroChequeEmis()))) {
            details.add(new DetailReprise("DOUBLON", dto.numeroChequeEmis(),
                    "Chèque paiement déjà enregistré"));
            return ResultatImport.DOUBLON;
        }

        // ── 2. Résolution du sinistre ────────────────────────
        Sinistre sinistre = sinistreRepository
                .findByNumeroSinistreManuel(clean(dto.numeroSinistreManuel()))
                .orElse(null);

        if (sinistre == null) {
            Organisme org = resoudreOrganisme(dto.organismePayeurCode());
            sinistre = creerSinistreMinimal(dto, org, loginAuteur);
            sinistreRepository.save(sinistre);
            details.add(new DetailReprise("SINISTRE_CREE", dto.numeroChequeEmis(),
                    "Sinistre minimal créé : " + dto.numeroSinistreManuel()));
        }

        // ── 3. Résolution / création victime bénéficiaire ────
        Victime beneficiaireVictime = victimeRepository
                .findAllBySinistre(sinistre.getSinistreTrackingId())
                .stream()
                .filter(v -> nomCorrespond(v, dto.beneficiaire()))
                .findFirst()
                .orElse(null);

        if (beneficiaireVictime == null) {
            beneficiaireVictime = creerVictimeMinimale(dto.beneficiaire(), sinistre, loginAuteur);
            victimeRepository.save(beneficiaireVictime);
            details.add(new DetailReprise("VICTIME_CREEE", dto.numeroChequeEmis(),
                    "Victime minimale créée : " + dto.beneficiaire()));
        }

        // ── 4. Résolution encaissement lié ───────────────────
        Organisme organismePayeur = resoudreOrganisme(dto.organismePayeurCode());
        List<Encaissement> encaissementsLies = new ArrayList<>();
        if (dto.numeroChequeEncaissement() != null && !dto.numeroChequeEncaissement().isBlank()) {
            encaissementRepository
                    .findByNumeroChequeAndOrganismeEmetteur(
                            clean(dto.numeroChequeEncaissement()), organismePayeur)
                    .ifPresent(encaissementsLies::add);
        }

        // ── 5. Calcul montant ────────────────────────────────
        BigDecimal principal = coalesce(dto.principalPaye());
        BigDecimal fg = coalesce(dto.fgPaye());
        BigDecimal montant = principal.add(fg);
        // NUMERIC(15,2) → absolute value must be < 10^13
        if (montant.abs().compareTo(new BigDecimal("9999999999999.99")) > 0) {
            throw new IllegalArgumentException(
                    "Montant hors plage NUMERIC(15,2) : " + montant + " (principal=" + principal + ", fg=" + fg + ")");
        }

        // ── 5b. Validation date obligatoire ─────────────────
        LocalDate dateEmission = parseDate(dto.datePaiement());
        if (dateEmission == null) {
            throw new IllegalArgumentException(
                    "Date de paiement absente ou invalide : '" + dto.datePaiement() + "'");
        }

        // ── 6. Construction paiement ─────────────────────────
        Paiement paiement = Paiement.builder()
                .paiementTrackingId(UUID.randomUUID())
                .beneficiaire(clean(dto.beneficiaire()))
                .beneficiaireVictime(beneficiaireVictime)
                .numeroChequeEmis(clean(dto.numeroChequeEmis()))
                .montant(montant)
                .banqueCheque("N/A")
                .dateEmission(dateEmission)
                .datePaiement(dateEmission)
                .modePaiement(clean(dto.modePaiement()))
                .statut(StatutPaiement.PAYE)
                .encaissements(encaissementsLies)
                .sinistre(sinistre)
                .repriseHistorique(true)
                .createdBy(loginAuteur)
                .activeData(true)
                .deletedData(false)
                .build();

        paiement.setNumeroPaiement(
                numeroOperationGenerator.genererNumero(TypeOperationFinanciere.REGLEMENT_TECHNIQUE, sinistre));
        paiementRepository.save(paiement);
        return ResultatImport.IMPORTE;
    }

    // ─── Victime minimale pour reprise paiements ────────────────────

    /**
     * Crée une Victime minimale à partir du nom brut du bénéficiaire.
     * Les champs NOT NULL sont renseignés avec des valeurs par défaut :
     * - prenoms → "." (placeholder visible)
     * - dateNaissance → 1900-01-01 (à corriger manuellement)
     * - sexe → "M" (défaut)
     * - statutActivite → SANS_EMPLOI (défaut neutre)
     */
    private Victime creerVictimeMinimale(String nomComplet, Sinistre sinistre, String loginAuteur) {
        // Enregistrer le nom tel quel dans le champ nom.
        // prenoms = "." comme placeholder visible — à compléter manuellement.
        String nom = (nomComplet != null && !nomComplet.isBlank()) ? nomComplet.trim() : "INCONNU";

        return Victime.builder()
                .victimeTrackingId(UUID.randomUUID())
                .nom(nom)
                .prenoms(".") // placeholder — à compléter manuellement
                .dateNaissance(java.time.LocalDate.of(1900, 1, 1)) // placeholder
                .sexe("M")
                .typeVictime(com.ossanasur.cbconnect.common.enums.TypeVictime.NEUTRE)
                .statutVictime(com.ossanasur.cbconnect.common.enums.StatutVictime.NEUTRE)
                .statutActivite(StatutActivite.SANS_EMPLOI)
                .revenuMensuel(BigDecimal.ZERO)
                .sinistre(sinistre)
                .createdBy(loginAuteur)
                .activeData(true)
                .deletedData(false)
                .build();
    }

    /**
     * Vérifie si le nom complet d'une Victime correspond au nom du bénéficiaire.
     * Comparaison insensible à la casse sur les tokens principaux.
     */
    private boolean nomCorrespond(Victime v, String nomBeneficiaire) {
        if (nomBeneficiaire == null || v.getNom() == null)
            return false;
        String complet = (v.getNom() + " " + (v.getPrenoms() != null ? v.getPrenoms() : ""))
                .trim().toLowerCase();
        return complet.equals(nomBeneficiaire.trim().toLowerCase())
                || complet.contains(nomBeneficiaire.trim().toLowerCase())
                || nomBeneficiaire.trim().toLowerCase().contains(v.getNom().toLowerCase());
    }

    // ─── Helper pour encaissement ────────────────────────────────────
    // Méthode à ajouter dans EncaissementRepository :
    //
    // Optional<Encaissement> findByNumeroChequeAndOrganismeEmetteur(
    // String numeroCheque, Organisme organismeEmetteur);

    // ─── Adapter creerSinistreMinimal pour accepter PaiementRepriseDto ──
    // (surcharge — même logique que celle pour encaissements)
    /**
     * Création d'un sinistre minimal depuis un paiement.
     * Applique la même sémantique pays/organismes que pour les encaissements
     * (cf. creerSinistreMinimal(EncaissementRepriseDto)).
     *
     * Le typeSinistre est :
     *  1. pris dans dto.lieuSurvenance() si fourni ("T" ou "E")
     *  2. sinon déduit du format de numeroSinistreManuel :
     *     "AAAA/NNN/PAYS"       → ET (commence par 4 chiffres)
     *     "PREF/NNN/PAYS/AAAA"  → TE (commence par lettres)
     *  3. fallback conservateur : SURVENU_TOGO
     */
    private Sinistre creerSinistreMinimal(PaiementRepriseDto dto,
            Organisme organisme,
            String loginAuteur) {
        String numSinistre = clean(dto.numeroSinistreManuel());
        if (numSinistre == null || numSinistre.isBlank()) {
            throw new IllegalArgumentException("Numéro sinistre manuel absent — paiement ignoré");
        }

        TypeSinistre type = deduireTypeSinistre(dto.lieuSurvenance(), numSinistre);

        Pays paysTG = resoudrePays("TG");
        Pays paysOrg = extrairePaysDepuisOrganisme(dto.organismePayeurCode());

        Pays paysGestionnaire, paysEmetteur;
        if (type == TypeSinistre.SURVENU_TOGO) {
            paysGestionnaire = paysTG;
            paysEmetteur     = (paysOrg != null && !isTG(paysOrg)) ? paysOrg : null;
        } else {
            paysGestionnaire = (paysOrg != null && !isTG(paysOrg)) ? paysOrg : null;
            paysEmetteur     = paysTG;
        }
        if (paysGestionnaire == null) paysGestionnaire = paysTG;

        Organisme homologue = resoudreOrganismeHomologueEtranger(paysEmetteur, paysGestionnaire);

        Assure assure = creerAssureMinimal(dto.assureNomComplet(), organisme, loginAuteur);
        assureRepository.save(assure);

        return Sinistre.builder()
                .sinistreTrackingId(UUID.randomUUID())
                .numeroSinistreManuel(numSinistre)
                .numeroSinistreLocal(numSinistre)
                .numeroSinistreHomologue(clean(dto.numeroSinistreHomologue()))
                .typeSinistre(type)
                .typeDommage(TypeDommage.MIXTE)
                .dateAccident(parseDate(dto.dateSinistre()))
                .dateDeclaration(parseDate(dto.dateSinistre()))
                .assure(assure)
                .paysGestionnaire(paysGestionnaire)
                .paysEmetteur(paysEmetteur)
                .organismeMembre(organisme)
                .organismeHomologue(homologue)
                .assureurDeclarant(clean(dto.organismePayeurCode()))
                .statut(StatutSinistre.NOUVEAU)
                .repriseHistorique(true)
                .createdBy(loginAuteur)
                .activeData(true)
                .deletedData(false)
                .build();
    }

    /**
     * Déduit le typeSinistre en priorité depuis lieuSurvenance, sinon depuis
     * le format du numéro manuel.
     */
    private TypeSinistre deduireTypeSinistre(String lieuSurvenance, String numeroSinistreManuel) {
        if (lieuSurvenance != null) {
            if ("T".equalsIgnoreCase(lieuSurvenance.trim())) return TypeSinistre.SURVENU_TOGO;
            if ("E".equalsIgnoreCase(lieuSurvenance.trim())) return TypeSinistre.SURVENU_ETRANGER;
        }
        if (numeroSinistreManuel != null) {
            String s = numeroSinistreManuel.trim();
            // ET : commence par 4 chiffres (année) "2024/001/BJ"
            if (s.matches("^\\d{4}/.*")) return TypeSinistre.SURVENU_TOGO;
            // TE : commence par lettres "TE/001/BJ/2024"
            if (s.matches("^[A-Za-z]+/.*")) return TypeSinistre.SURVENU_ETRANGER;
        }
        return TypeSinistre.SURVENU_TOGO; // fallback conservateur
    }

    /**
     * "BNCB BF" / "BNCB-BF" / "BNCB BF" → "BCB-BF". Renvoie null si le pattern
     * ne matche pas (l'appelant tombe alors dans le flux d'erreur normal).
     */
    private String normaliserCodeBureauHomologue(String raw) {
        String[] parts = raw.split("[\\s-]+");
        if (parts.length == 2 && "BNCB".equalsIgnoreCase(parts[0])) {
            return "BCB-" + parts[1].toUpperCase();
        }
        return null;
    }

    private TypeDommage parseTypeDommage(String val) {
        if (val == null)
            return TypeDommage.MATERIEL;
        return switch (val.toUpperCase().trim()) {
            case "CORPOREL" -> TypeDommage.CORPOREL;
            case "MIXTE" -> TypeDommage.MIXTE;
            default -> TypeDommage.MATERIEL;
        };
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank())
            return null;
        try {
            return LocalDate.parse(dateStr.substring(0, 10));
        } catch (Exception e) {
            return null;
        }
    }

    private String clean(String s) {
        return s != null ? s.trim().replace("\u00a0", "").strip() : null;
    }
}