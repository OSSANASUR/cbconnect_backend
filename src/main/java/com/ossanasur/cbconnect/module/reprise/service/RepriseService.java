package com.ossanasur.cbconnect.module.reprise.service;

import com.ossanasur.cbconnect.common.enums.StatutSinistre;
import com.ossanasur.cbconnect.common.enums.TypeDommage;
import com.ossanasur.cbconnect.common.enums.TypeOrganisme;
import com.ossanasur.cbconnect.common.enums.TypeSinistre;
import com.ossanasur.cbconnect.module.auth.entity.Organisme;
import com.ossanasur.cbconnect.module.auth.repository.OrganismeRepository;
import com.ossanasur.cbconnect.module.pays.entity.Pays;
import com.ossanasur.cbconnect.module.pays.repository.PaysRepository;
import com.ossanasur.cbconnect.module.reprise.dto.RapportReprise;
import com.ossanasur.cbconnect.module.reprise.dto.RepriseOrganismesRequest;
import com.ossanasur.cbconnect.module.reprise.dto.RepriseSinistresRequest;
import com.ossanasur.cbconnect.module.reprise.dto.RapportReprise.DetailReprise;
import com.ossanasur.cbconnect.module.reprise.dto.RepriseOrganismesRequest.OrganismeRepriseDto;
import com.ossanasur.cbconnect.module.reprise.dto.RepriseSinistresRequest.SinistreRepriseDto;
import com.ossanasur.cbconnect.module.sinistre.entity.Assure;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import com.ossanasur.cbconnect.module.sinistre.repository.AssureRepository;
import com.ossanasur.cbconnect.module.sinistre.repository.SinistreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
 *   complet et de l'immatriculation issus du fichier Excel
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
    // Sinistre.assure est nullable=false → on doit persister un Assure avant le sinistre.
    private final AssureRepository assureRepository;

    // Cache en mémoire pour la session d'import (évite N requêtes SQL par sinistre)
    private final Map<String, Pays> cacheP = new HashMap<>();
    private final Map<String, Organisme> cacheO = new HashMap<>();

    // ─── Import sinistres ────────────────────────────────────────────────────

    @Transactional
    public RapportReprise importerSinistres(RepriseSinistresRequest req, String loginAuteur) {
        int importes = 0, doublons = 0, erreurs = 0;
        List<DetailReprise> details = new ArrayList<>();

        for (SinistreRepriseDto dto : req.sinistres()) {
            try {
                // ── Doublon ──
                if (sinistreRepository.existsByNumeroSinistreManuel(dto.numeroSinistreManuel())) {
                    doublons++;
                    details.add(new DetailReprise("DOUBLON", dto.numeroSinistreManuel(),
                            "Déjà présent en base"));
                    continue;
                }

                // ── Résolution pays émetteur ──
                Pays paysEmetteur = resoudrePays(dto.paysEmetteurCode());
                if (paysEmetteur == null) {
                    erreurs++;
                    details.add(new DetailReprise("ERROR", dto.numeroSinistreManuel(),
                            "Pays émetteur introuvable : " + dto.paysEmetteurCode()));
                    continue;
                }

                // ── Résolution pays gestionnaire (Togo = TG par défaut) ──
                Pays paysGestionnaire = resoudrePays(
                        dto.paysGestionnaireCode() != null ? dto.paysGestionnaireCode() : "TG");

                // ── Résolution organisme membre (assureur déclarant) ──
                Organisme organismeMembre = resoudreOrganisme(dto.assureurDeclarant());

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
                        // Numéros
                        .numeroSinistreManuel(dto.numeroSinistreManuel())
                        .numeroSinistreLocal(numLocal)
                        .numeroSinistreEcarteBrune(clean(dto.numeroSinistreEcarteBrune()))
                        .numeroSinistreHomologue(clean(dto.numeroSinistreHomologue()))
                        // Types
                        .typeSinistre(TypeSinistre.valueOf(dto.typeSinistre()))
                        .typeDommage(parseTypeDommage(dto.typeDommage()))
                        // Dates : en reprise, on aligne date déclaration sur date accident
                        // car le fichier BNCB ne distingue pas les deux.
                        .dateAccident(parseDate(dto.dateAccident()))
                        .dateDeclaration(parseDate(dto.dateAccident()))
                        // Assuré persisté juste au-dessus
                        .assure(assure)
                        // Assureur (champs bruts conservés pour audit / traçabilité)
                        .assureurDeclarant(clean(dto.assureurDeclarant()))
                        .numeroPoliceAssureur(clean(dto.numeroPoliceAssureur()))
                        // Relations
                        .paysEmetteur(paysEmetteur)
                        .paysGestionnaire(paysGestionnaire)
                        .organismeMembre(organismeMembre)
                        // Statut initial (enum, pas String)
                        .statut(StatutSinistre.NOUVEAU)
                        .repriseHistorique(true)
                        // Audit
                        .createdBy(loginAuteur)
                        .activeData(true)
                        .deletedData(false)
                        .build();

                sinistreRepository.save(sinistre);
                importes++;

            } catch (Exception e) {
                erreurs++;
                log.warn("[REPRISE] Erreur import sinistre {} : {}", dto.numeroSinistreManuel(), e.getMessage());
                details.add(new DetailReprise("ERROR", dto.numeroSinistreManuel(), e.getMessage()));
            }
        }

        return new RapportReprise(req.sinistres().size(), importes, doublons, erreurs,
                details.isEmpty() ? List.of() : details);
    }

    // ─── Import organismes ───────────────────────────────────────────────────

    @Transactional
    public RapportReprise importerOrganismes(RepriseOrganismesRequest req, String loginAuteur) {
        int importes = 0, doublons = 0, erreurs = 0;
        List<DetailReprise> details = new ArrayList<>();
        // Le fichier Excel groupe les organismes par pays via une ligne d'en-tête :
        // on mémorise le dernier pays vu pour l'appliquer aux lignes suivantes.
        String currentPays = null;

        for (OrganismeRepriseDto dto : req.organismes()) {
            try {
                // Doublon sur code
                if (organismeRepository.existsByCode(dto.code())) {
                    doublons++;
                    continue;
                }

                // Résolution pays si renseigné
                Pays pays = null;
                String paysDtoNom = dto.pays();
                if (paysDtoNom != null)
                    currentPays = paysDtoNom;

                // Chercher par libellé
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

                // Organisme.email est UNIQUE + NOT NULL, mais la reprise Excel ne
                // fournit pas d'email → on génère un email synthétique basé sur le
                // code (pourra être complété manuellement après import).
                String emailSynthetique = "reprise-" +
                        dto.code().trim().toLowerCase().replace(" ", "-") +
                        "@cbconnect.local";

                Organisme org = Organisme.builder()
                        .organismeTrackingId(UUID.randomUUID())
                        .raisonSociale(dto.raisonSociale())
                        .code(dto.code())
                        // Enum, pas String
                        .typeOrganisme(TypeOrganisme.COMPAGNIE_MEMBRE)
                        // Organisme n'a pas de relation @ManyToOne vers Pays :
                        // on alimente les trois champs de référence disponibles.
                        .paysId(pays != null ? pays.getHistoriqueId() : null)
                        .codePaysBCB(pays != null ? pays.getCodeCarteBrune() : null)
                        .codePays(pays != null ? pays.getCodeIso() : null)
                        .email(emailSynthetique)
                        // Le champ s'appelle `active` (pas `actif`)
                        .active(true)
                        .repriseHistorique(true)
                        .createdBy(loginAuteur)
                        .activeData(true)
                        .deletedData(false)
                        .build();

                organismeRepository.save(org);
                importes++;
                cacheO.put(dto.code().toUpperCase(), org);

            } catch (Exception e) {
                erreurs++;
                log.warn("[REPRISE] Erreur import organisme {} : {}", dto.code(), e.getMessage());
                details.add(new DetailReprise("ERROR", dto.code(), e.getMessage()));
            }
        }

        return new RapportReprise(req.organismes().size(), importes, doublons, erreurs,
                details.isEmpty() ? List.of() : details);
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
        return Assure.builder()
                .assureTrackingId(UUID.randomUUID())
                .nomAssure(nomComplet)
                .nomComplet(nomComplet)
                .immatriculation(clean(dto.assureImmatriculation()))
                .organisme(organisme)
                .createdBy(loginAuteur)
                .activeData(true)
                .deletedData(false)
                .build();
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
     * Résout un organisme par son code assureur.
     * Ex : "SUNU BJ" → cherche code="SUNU BJ" ou raisonSociale contenant "SUNU BJ".
     */
    private Organisme resoudreOrganisme(String assureurDeclarant) {
        if (assureurDeclarant == null || assureurDeclarant.isBlank())
            return null;
        String key = assureurDeclarant.trim().toUpperCase();
        if (cacheO.containsKey(key))
            return cacheO.get(key);
        Organisme o = organismeRepository.findByCodeIgnoreCase(assureurDeclarant.trim())
                .or(() -> organismeRepository.findByRaisonSocialeContainingIgnoreCase(assureurDeclarant.trim()))
                .orElse(null);
        if (o != null)
            cacheO.put(key, o);
        return o;
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
