package com.ossanasur.cbconnect.module.sinistre.service.impl;

import com.ossanasur.cbconnect.common.enums.*;
import com.ossanasur.cbconnect.exception.*;
import com.ossanasur.cbconnect.historique.SinistreVersioningService;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.module.auth.repository.OrganismeRepository;
import com.ossanasur.cbconnect.module.auth.repository.UtilisateurRepository;
import com.ossanasur.cbconnect.module.pays.entity.Pays;
import com.ossanasur.cbconnect.module.pays.repository.PaysRepository;
import com.ossanasur.cbconnect.module.sinistre.dto.request.ConfirmationGarantieRequest;
import com.ossanasur.cbconnect.module.sinistre.dto.request.MiseEnArbitrageRequest;
import com.ossanasur.cbconnect.module.sinistre.dto.request.MiseEnContentieuxRequest;
import com.ossanasur.cbconnect.module.sinistre.dto.request.SinistreRequest;
import com.ossanasur.cbconnect.module.finance.repository.EncaissementRepository;
import com.ossanasur.cbconnect.module.finance.repository.PaiementRepository;
import com.ossanasur.cbconnect.module.sinistre.dto.response.EncaissementStatusResponse;
import com.ossanasur.cbconnect.module.sinistre.dto.response.SinistreResponse;
import com.ossanasur.cbconnect.module.sinistre.entity.*;
import com.ossanasur.cbconnect.module.sinistre.mapper.SinistreMapper;
import com.ossanasur.cbconnect.module.ged.service.OssanGedClientService;
import com.ossanasur.cbconnect.module.sinistre.repository.*;
import com.ossanasur.cbconnect.module.sinistre.service.SinistreService;
import com.ossanasur.cbconnect.utils.DataResponse;
import com.ossanasur.cbconnect.utils.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SinistreServiceImpl implements SinistreService {
    private final SinistreRepository sinistreRepository;
    private final AssureRepository assureRepository;
    private final PaysRepository paysRepository;
    private final OrganismeRepository organismeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EntiteConstatRepository entiteConstatRepository;
    private final SinistreVersioningService versioningService;
    private final SinistreMapper sinistreMapper;
    private final OssanGedClientService gedService;
    private final EncaissementRepository encaissementRepository;
    private final PaiementRepository paiementRepository;

    @Override
    @Transactional
    public DataResponse<SinistreResponse> create(SinistreRequest r, String loginAuteur) {
        Pays paysG = paysRepository.findActiveByTrackingId(r.paysGestionnaireTrackingId())
                .orElseThrow(() -> new RessourceNotFoundException("Pays gestionnaire introuvable"));
        Assure assure = assureRepository.findActiveByTrackingId(r.assureTrackingId())
                .orElseThrow(() -> new RessourceNotFoundException("Assure introuvable"));

        // Generation du numero local — nomenclature BNCB Togo :
        //   ET (SURVENU_TOGO)     : "ANNEE / ORDRE / CODE_PAYS_EMETTEUR"         ex: 2026 / 0234 / BF
        //   TE (SURVENU_ETRANGER) : "CODE_CIE / ORDRE / CODE_PAYS_GESTIONNAIRE / ANNEE" ex: NS / 0234 / BF / 2026
        int annee = LocalDate.now().getYear();
        long seq = sinistreRepository.countByAnneeAndType(annee, r.typeSinistre()) + 1;
        String ordre = String.format("%04d", seq);
        String numeroLocal;
        if (r.typeSinistre() == TypeSinistre.SURVENU_TOGO) {
            String codePaysEmetteur = "TG";
            if (r.paysEmetteurTrackingId() != null) {
                Pays paysE = paysRepository.findActiveByTrackingId(r.paysEmetteurTrackingId()).orElse(null);
                if (paysE != null && paysE.getCodeCarteBrune() != null) {
                    codePaysEmetteur = paysE.getCodeCarteBrune();
                }
            }
            numeroLocal = annee + " / " + ordre + " / " + codePaysEmetteur;
        } else {
            String codeCie = assure.getOrganisme() != null && assure.getOrganisme().getCode() != null
                    ? assure.getOrganisme().getCode() : "BNCB";
            String codePaysGest = paysG.getCodeCarteBrune() != null ? paysG.getCodeCarteBrune() : "TG";
            numeroLocal = codeCie + " / " + ordre + " / " + codePaysGest + " / " + annee;
        }

        Sinistre s = Sinistre.builder()
                .sinistreTrackingId(UUID.randomUUID()).numeroSinistreLocal(numeroLocal)
                .numeroSinistreManuel(r.numeroSinistreManuel()).numeroSinistreHomologue(r.numeroSinistreHomologue())
                .numeroSinistreEcarteBrune(r.numeroSinistreEcarteBrune())
                .typeSinistre(r.typeSinistre()).typeDommage(r.typeDommage()).statut(StatutSinistre.NOUVEAU)
                .dateAccident(r.dateAccident())
                .dateDeclaration(r.dateDeclaration() != null ? r.dateDeclaration() : LocalDate.now())
                .lieuAccident(r.lieuAccident()).agglomeration(r.agglomeration())
                .positionRc(r.positionRc())
                /* V22 extension */
                .heureAccident(r.heureAccident())
                .ville(r.ville()).commune(r.commune())
                .provenance(r.provenance()).destination(r.destination())
                .circonstances(r.circonstances())
                .pvEtabli(Boolean.TRUE.equals(r.pvEtabli()))
                .dateEffet(r.dateEffet()).dateEcheance(r.dateEcheance())
                .conducteurEstAssure(r.conducteurEstAssure() == null || r.conducteurEstAssure())
                .conducteurNom(r.conducteurNom()).conducteurPrenom(r.conducteurPrenom())
                .conducteurDateNaissance(r.conducteurDateNaissance())
                .conducteurNumeroPermis(r.conducteurNumeroPermis())
                .conducteurCategoriesPermis(r.conducteurCategoriesPermis() == null
                        ? null : String.join(",", r.conducteurCategoriesPermis()))
                .conducteurDateDelivrance(r.conducteurDateDelivrance())
                .conducteurLieuDelivrance(r.conducteurLieuDelivrance())
                .declarantNom(r.declarantNom()).declarantPrenom(r.declarantPrenom())
                .declarantTelephone(r.declarantTelephone()).declarantQualite(r.declarantQualite())
                .numeroSinistreAssureur(r.numeroSinistreAssureur())
                .paysGestionnaire(paysG).assure(assure)
                .createdBy(loginAuteur).activeData(true).deletedData(false).fromTable(TypeTable.SINISTRE)
                .build();

        if (r.paysEmetteurTrackingId() != null)
            paysRepository.findActiveByTrackingId(r.paysEmetteurTrackingId()).ifPresent(s::setPaysEmetteur);
        if (r.organismeMembreTrackingId() != null)
            organismeRepository.findActiveByTrackingId(r.organismeMembreTrackingId()).ifPresent(s::setOrganismeMembre);
        if (r.organismeHomologueTrackingId() != null)
            organismeRepository.findActiveByTrackingId(r.organismeHomologueTrackingId())
                    .ifPresent(s::setOrganismeHomologue);
        if (r.redacteurTrackingId() != null)
            utilisateurRepository
                    .findByUtilisateurTrackingIdAndActiveDataTrueAndDeletedDataFalse(r.redacteurTrackingId())
                    .ifPresent(s::setRedacteur);
        if (r.entiteConstatTrackingId() != null)
            entiteConstatRepository.findActiveByTrackingId(r.entiteConstatTrackingId())
                    .ifPresent(s::setEntiteConstat);
        if (r.assureursSecondairesTrackingIds() != null && !r.assureursSecondairesTrackingIds().isEmpty()) {
            java.util.Set<com.ossanasur.cbconnect.module.auth.entity.Organisme> secs = new java.util.HashSet<>();
            for (UUID orgId : r.assureursSecondairesTrackingIds()) {
                organismeRepository.findActiveByTrackingId(orgId).ifPresent(secs::add);
            }
            s.setAssureursSecondaires(secs);
        }

        Sinistre saved = sinistreRepository.save(s);

        // Création automatique du dossier GED pour ce sinistre.
        // Idempotent et best-effort : l'échec GED ne bloque pas la création du sinistre.
        try {
            gedService.creerDossierSinistre(saved.getSinistreTrackingId(), loginAuteur);
        } catch (Exception e) {
            log.warn("Dossier GED non créé pour le sinistre {} (création PV-upload dégradée) : {}",
                    saved.getNumeroSinistreLocal(), e.getMessage());
        }

        return DataResponse.created("Sinistre cree : " + numeroLocal, sinistreMapper.toResponse(saved));
    }

    @Override
    @Transactional
    public DataResponse<SinistreResponse> update(UUID id, SinistreRequest r, String loginAuteur) {
        // Contrainte métier : la déclaration n'est modifiable qu'aux statuts
        // NOUVEAU ou VALIDE (avant confirmation de garantie). Au-delà, les étapes
        // aval (PV, RC, offre…) dépendent de la cohérence des données.
        Sinistre existing = versioningService.getActiveVersion(id);
        StatutSinistre st = existing.getStatut();
        if (st != StatutSinistre.NOUVEAU && st != StatutSinistre.VALIDE) {
            throw new BadRequestException(
                    "Modification interdite : le dossier est au statut " + st +
                            ". Seuls les statuts NOUVEAU et VALIDE autorisent l'édition de la déclaration.");
        }
        Sinistre updated = versioningService.createVersion(id, r, loginAuteur);
        return DataResponse.success("Sinistre mis a jour", sinistreMapper.toResponse(updated));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<SinistreResponse> getByTrackingId(UUID id) {
        return DataResponse.success(sinistreMapper.toResponse(versioningService.getActiveVersion(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<SinistreResponse> getAll(int page, int size) {
        return PaginatedResponse.fromPage(sinistreRepository.findAllActive(
                PageRequest.of(page, size, Sort.by("dateDeclaration").descending()))
                .map(sinistreMapper::toResponse), "Liste des sinistres");
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<SinistreResponse> search(String query, int page, int size) {
        if (query == null || query.trim().length() < 2) {
            return PaginatedResponse.fromPage(
                    org.springframework.data.domain.Page.empty(PageRequest.of(page, Math.max(size, 1))),
                    "Aucun résultat");
        }
        return PaginatedResponse.fromPage(
                sinistreRepository.search(query.trim(),
                        PageRequest.of(page, Math.min(Math.max(size, 1), 50)))
                        .map(sinistreMapper::toResponse),
                "Résultats recherche sinistre");
    }

    @Override
    @Transactional
    public DataResponse<Void> changerStatut(UUID id, String nouveauStatut, String loginAuteur) {
        Sinistre s = versioningService.getActiveVersion(id);
        StatutSinistre statut;
        try {
            statut = StatutSinistre.valueOf(nouveauStatut.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Statut invalide : " + nouveauStatut);
        }
        s.setStatut(statut);
        s.setUpdatedBy(loginAuteur);
        sinistreRepository.save(s);
        return DataResponse.success("Statut mis a jour : " + statut, null);
    }

    @Override
    @Transactional
    public DataResponse<Void> assignerRedacteur(UUID sinistreId, UUID redacteurId, String loginAuteur) {
        Sinistre s = versioningService.getActiveVersion(sinistreId);
        Utilisateur r = utilisateurRepository
                .findByUtilisateurTrackingIdAndActiveDataTrueAndDeletedDataFalse(redacteurId)
                .orElseThrow(() -> new RessourceNotFoundException("Redacteur introuvable"));
        s.setRedacteur(r);
        s.setUpdatedBy(loginAuteur);
        sinistreRepository.save(s);
        return DataResponse.success("Redacteur assigne", null);
    }

    @Override
    @Transactional
    public DataResponse<Void> delete(UUID id, String loginAuteur) {
        versioningService.softDelete(id, loginAuteur);
        return DataResponse.success("Sinistre supprime", null);
    }

    /*
     * V27 : la gestion de la position RC est portée par
     * VictimeService#executerActionRc,
     * adversaire par adversaire. L'ancienne méthode changerPositionRc sur sinistre
     * est
     * remplacée par une erreur métier explicite pour guider les éventuels clients
     * résiduels.
     */
    @Override
    @Transactional
    public DataResponse<SinistreResponse> changerPositionRc(UUID id, String positionRc, String loginAuteur) {
        throw new BadRequestException(
                "La position RC se gere desormais par adversaire : " +
                        "PATCH /v1/victimes/{adversaireId}/position-rc avec body {action, pourcentage, motifRejet}.");
    }

    @Override
    @Transactional
    public DataResponse<SinistreResponse> confirmerGarantie(UUID id, ConfirmationGarantieRequest r,
            String loginAuteur) {
        Sinistre s = sinistreRepository.findActiveByTrackingId(id)
                .orElseThrow(() -> new RessourceNotFoundException("Sinistre introuvable"));

        s.setGarantieAcquise(r.garantieAcquise());
        s.setReferenceGarantie(r.referenceGarantie());
        s.setDateConfirmationGarantie(
                r.dateConfirmationGarantie() != null ? r.dateConfirmationGarantie() : java.time.LocalDate.now());
        s.setObservationsGarantie(r.observationsGarantie());
        s.setCourrierNonGarantieRef(r.courrierNonGarantieRef());
        s.setCourrierNonGarantieDate(r.courrierNonGarantieDate());
        // Garantie acquise → ATTENTE_PV (instruction démarre) ; non acquise → dossier
        // bloqué en GARANTIE_NON_ACQUISE
        s.setStatut(Boolean.TRUE.equals(r.garantieAcquise())
                ? StatutSinistre.ATTENTE_PV
                : StatutSinistre.GARANTIE_NON_ACQUISE);
        s.setUpdatedBy(loginAuteur);

        Sinistre saved = sinistreRepository.save(s);
        String msg = Boolean.TRUE.equals(r.garantieAcquise())
                ? "Garantie confirmee"
                : "Garantie non acquise enregistree";
        return DataResponse.success(msg, sinistreMapper.toResponse(saved));
    }

    /*
     * ═════════ Passages en CONTENTIEUX / ARBITRAGE / sortie vers BAP ═════════
     * Règles métier (doc OpenL) :
     * - Depuis n'importe quel statut, le SE peut passer un dossier en CONTENTIEUX
     * (procédure judiciaire) ou ARBITRAGE (instance arbitrale / commission).
     * - Le flag `estContentieux` reste vrai tant que le dossier est en
     * CONTENTIEUX. On le remet à faux à la sortie.
     * - `niveauJuridiction` et `dateProchaineAudience` sont persistés pour
     * alimenter les alertes d'audience côté Délais (CategorieActiviteDelai).
     * - Sortie de litige → statut BAP : le dossier rejoint la file normale,
     * est bon à payer (avec l'offre déjà calculée en amont).
     */

    @Override
    @Transactional
    public DataResponse<SinistreResponse> mettreEnContentieux(UUID id, MiseEnContentieuxRequest r, String loginAuteur) {
        Sinistre s = sinistreRepository.findActiveByTrackingId(id)
                .orElseThrow(() -> new RessourceNotFoundException("Sinistre introuvable"));
        if (s.getStatut() == StatutSinistre.CONTENTIEUX) {
            throw new BadRequestException("Le dossier est déjà en contentieux.");
        }
        if (s.getStatut() == StatutSinistre.CLOTURE) {
            throw new BadRequestException("Un dossier clôturé doit être rouvert avant d'être mis en contentieux.");
        }
        s.setEstContentieux(true);
        s.setNiveauJuridiction(r.niveauJuridiction());
        s.setDateProchaineAudience(r.dateProchaineAudience());
        if (r.observations() != null && !r.observations().isBlank()) {
            String entete = "[CONTENTIEUX " + LocalDate.now() + " – " + loginAuteur + "] ";
            String existing = s.getObservationsGarantie() == null ? "" : s.getObservationsGarantie() + "\n";
            s.setObservationsGarantie(existing + entete + r.observations());
        }
        s.setStatut(StatutSinistre.CONTENTIEUX);
        s.setUpdatedBy(loginAuteur);
        Sinistre saved = sinistreRepository.save(s);
        log.info("[WORKFLOW] Sinistre {} → CONTENTIEUX (juridiction={}, audience={})",
                saved.getNumeroSinistreLocal(), r.niveauJuridiction(), r.dateProchaineAudience());
        return DataResponse.success("Dossier mis en contentieux", sinistreMapper.toResponse(saved));
    }

    @Override
    @Transactional
    public DataResponse<SinistreResponse> mettreEnArbitrage(UUID id, MiseEnArbitrageRequest r, String loginAuteur) {
        Sinistre s = sinistreRepository.findActiveByTrackingId(id)
                .orElseThrow(() -> new RessourceNotFoundException("Sinistre introuvable"));
        if (s.getStatut() == StatutSinistre.ARBITRAGE) {
            throw new BadRequestException("Le dossier est déjà en arbitrage.");
        }
        if (s.getStatut() == StatutSinistre.CLOTURE) {
            throw new BadRequestException("Un dossier clôturé doit être rouvert avant d'être mis en arbitrage.");
        }
        if (r.observations() != null && !r.observations().isBlank()) {
            String entete = "[ARBITRAGE " + LocalDate.now() + " – " + loginAuteur + "] ";
            String existing = s.getObservationsGarantie() == null ? "" : s.getObservationsGarantie() + "\n";
            s.setObservationsGarantie(existing + entete + r.observations());
        }
        s.setStatut(StatutSinistre.ARBITRAGE);
        s.setUpdatedBy(loginAuteur);
        Sinistre saved = sinistreRepository.save(s);
        log.info("[WORKFLOW] Sinistre {} → ARBITRAGE (saisine={})",
                saved.getNumeroSinistreLocal(), r.dateSaisineArbitrage());
        return DataResponse.success("Dossier mis en arbitrage", sinistreMapper.toResponse(saved));
    }

    @Override
    @Transactional
    public DataResponse<SinistreResponse> sortirDuLitige(UUID id, String loginAuteur) {
        Sinistre s = sinistreRepository.findActiveByTrackingId(id)
                .orElseThrow(() -> new RessourceNotFoundException("Sinistre introuvable"));
        if (s.getStatut() != StatutSinistre.CONTENTIEUX && s.getStatut() != StatutSinistre.ARBITRAGE) {
            throw new BadRequestException(
                    "Sortie de litige possible uniquement depuis CONTENTIEUX ou ARBITRAGE (statut actuel : "
                            + s.getStatut() + ").");
        }
        s.setEstContentieux(false);
        s.setStatut(StatutSinistre.BAP);
        s.setUpdatedBy(loginAuteur);
        Sinistre saved = sinistreRepository.save(s);
        log.info("[WORKFLOW] Sinistre {} sort de litige → BAP", saved.getNumeroSinistreLocal());
        return DataResponse.success("Dossier sorti du litige – passé en BAP", sinistreMapper.toResponse(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<EncaissementStatusResponse> getEncaissementStatus(UUID sinistreTrackingId) {

        sinistreRepository.findActiveByTrackingId(sinistreTrackingId)
                .orElseThrow(() -> new RessourceNotFoundException("Sinistre introuvable"));

        boolean hasAny = encaissementRepository.existsNonAnnuleBySinistre(sinistreTrackingId);
        BigDecimal totalEncaisse = nz(encaissementRepository.sumMontantEncaisseBySinistre(sinistreTrackingId));
        BigDecimal totalEngage = nz(paiementRepository.sumMontantActifBySinistre(sinistreTrackingId));
        BigDecimal couverture = totalEncaisse.subtract(totalEngage);

        boolean regleAOk = hasAny;
        boolean regleBOk = totalEncaisse.signum() > 0;
        boolean regleCOk = couverture.signum() >= 0;
        boolean hasEncaisse = regleBOk;

        String message = null;
        if (!regleAOk) {
            message = "Aucun encaissement enregistré pour ce sinistre. La création d'un règlement est bloquée.";
        } else if (!regleBOk) {
            message = "Aucun chèque encore crédité en banque. Le règlement comptable sera bloqué.";
        } else if (!regleCOk) {
            message = String.format(
                    "Couverture insuffisante : encaissé %s FCFA, engagé %s FCFA. La validation comptable est bloquée.",
                    totalEncaisse.toPlainString(), totalEngage.toPlainString());
        }

        return DataResponse.success(new EncaissementStatusResponse(
                hasAny, hasEncaisse, totalEncaisse, totalEngage, couverture,
                regleAOk, regleBOk, regleCOk, message));
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
