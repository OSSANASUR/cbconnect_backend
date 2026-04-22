package com.ossanasur.cbconnect.module.sinistre.service.impl;
import com.ossanasur.cbconnect.common.enums.*;
import com.ossanasur.cbconnect.exception.*;
import com.ossanasur.cbconnect.historique.VictimeVersioningService;
import com.ossanasur.cbconnect.module.pays.repository.PaysRepository;
import com.ossanasur.cbconnect.module.sinistre.dto.request.ActionRcRequest;
import com.ossanasur.cbconnect.module.sinistre.dto.request.VictimeRequest;
import com.ossanasur.cbconnect.module.sinistre.dto.response.VictimeResponse;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import com.ossanasur.cbconnect.module.sinistre.entity.Victime;
import com.ossanasur.cbconnect.module.sinistre.mapper.VictimeMapper;
import com.ossanasur.cbconnect.module.sinistre.repository.*;
import com.ossanasur.cbconnect.module.sinistre.service.VictimeService;
import com.ossanasur.cbconnect.utils.DataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@Service @RequiredArgsConstructor @Slf4j
public class VictimeServiceImpl implements VictimeService {
    private final VictimeRepository victimeRepository;
    private final SinistreRepository sinistreRepository;
    private final PaysRepository paysRepository;
    private final VictimeVersioningService versioningService;
    private final VictimeMapper victimeMapper;

    @Override @Transactional
    public DataResponse<VictimeResponse> create(VictimeRequest r, String loginAuteur) {
        var sinistre = sinistreRepository.findActiveByTrackingId(r.sinistreTrackingId())
            .orElseThrow(()->new RessourceNotFoundException("Sinistre introuvable"));
        TypeVictime tv = r.typeVictime() != null ? r.typeVictime() : TypeVictime.NEUTRE;
        Victime v = Victime.builder()
            .victimeTrackingId(UUID.randomUUID()).nom(r.nom()).prenoms(r.prenoms())
            .dateNaissance(r.dateNaissance()).sexe(r.sexe()).nationalite(r.nationalite())
            .statutActivite(r.statutActivite())
            .revenuMensuel(r.revenuMensuel()!=null?r.revenuMensuel():java.math.BigDecimal.ZERO)
            .typeVictime(tv).statutVictime(StatutVictime.NEUTRE)
            .estAdversaire(Boolean.TRUE.equals(r.estAdversaire()))
            .profession(r.profession())
            .typeDommage(r.typeDommage())
            .telephone(r.telephone())
            .numeroPermis(r.numeroPermis())
            .categoriesPermis(r.categoriesPermis() == null ? null : String.join(",", r.categoriesPermis()))
            .dateDelivrance(r.dateDelivrance())
            .lieuDelivrance(r.lieuDelivrance())
            .marqueVehicule(r.marqueVehicule())
            .modeleVehicule(r.modeleVehicule())
            .genreVehicule(r.genreVehicule())
            .couleurVehicule(r.couleurVehicule())
            .immatriculation(r.immatriculation())
            .numeroChassis(r.numeroChassis())
            .prochaineVT(r.prochaineVT())
            .capaciteVehicule(r.capaciteVehicule())
            .nbPersonnesABord(r.nbPersonnesABord())
            .proprietaireVehicule(r.proprietaireVehicule())
            .aRemorque(Boolean.TRUE.equals(r.aRemorque()))
            .assureurAdverse(r.assureurAdverse())
            .descriptionDegats(r.descriptionDegats())
            .blessesLegers(r.blessesLegers() != null ? r.blessesLegers() : 0)
            .blessesGraves(r.blessesGraves() != null ? r.blessesGraves() : 0)
            .deces(r.deces() != null ? r.deces() : 0)
            .sinistre(sinistre)
            .createdBy(loginAuteur).activeData(true).deletedData(false).fromTable(TypeTable.VICTIME)
            .build();
        if(r.paysResidenceTrackingId()!=null)
            paysRepository.findActiveByTrackingId(r.paysResidenceTrackingId()).ifPresent(v::setPaysResidence);
        return DataResponse.created("Victime enregistree", victimeMapper.toResponse(victimeRepository.save(v)));
    }

    @Override @Transactional
    public DataResponse<VictimeResponse> update(UUID id, VictimeRequest r, String loginAuteur) {
        return DataResponse.success("Victime mise a jour", victimeMapper.toResponse(versioningService.createVersion(id, r, loginAuteur)));
    }

    @Override @Transactional(readOnly=true)
    public DataResponse<VictimeResponse> getByTrackingId(UUID id) {
        return DataResponse.success(victimeMapper.toResponse(versioningService.getActiveVersion(id)));
    }

    @Override @Transactional(readOnly=true)
    public DataResponse<List<VictimeResponse>> getBySinistre(UUID sinistreId) {
        return DataResponse.success(victimeRepository.findAllBySinistre(sinistreId).stream()
            .map(victimeMapper::toResponse).collect(Collectors.toList()));
    }

    @Override @Transactional
    public DataResponse<Void> delete(UUID id, String loginAuteur) {
        versioningService.softDelete(id, loginAuteur);
        return DataResponse.success("Victime supprimee", null);
    }

    @Override @Transactional
    public DataResponse<Void> changerStatutVictime(UUID id, String statut, String loginAuteur) {
        Victime v = versioningService.getActiveVersion(id);
        try { v.setStatutVictime(StatutVictime.valueOf(statut.toUpperCase())); }
        catch(IllegalArgumentException e) { throw new BadRequestException("Statut victime invalide: " + statut); }
        v.setUpdatedBy(loginAuteur); victimeRepository.save(v);
        return DataResponse.success("Statut victime mis a jour", null);
    }

    /* ═══════════════════════════════════════════════════════════════════
       NÉGOCIATION RC PAR ADVERSAIRE (V27)
       Règles métier strictes : chaque action vérifie le statut source
       avant d'appliquer. TRANCHEE = immuable.
       ═══════════════════════════════════════════════════════════════════ */
    @Override @Transactional
    public DataResponse<VictimeResponse> executerActionRc(UUID adversaireId, ActionRcRequest r, String loginAuteur) {
        Victime adv = versioningService.getActiveVersion(adversaireId);
        if (!adv.isEstAdversaire()) {
            throw new BadRequestException("La position RC se gère uniquement sur un adversaire.");
        }
        if (adv.getPositionRc() == PositionRc.TRANCHEE) {
            throw new BadRequestException("Position RC déjà tranchée (" + adv.getPourcentageRcFinal() + "%) — immuable.");
        }

        LocalDateTime now = LocalDateTime.now();
        switch (r.action()) {
            case PROPOSER -> proposer(adv, r, now);
            case REJETER  -> rejeter(adv, r, now);
            case ACCEPTER -> accepter(adv, now);
            case TRANCHER -> trancher(adv, r, now);
        }
        adv.setUpdatedBy(loginAuteur);
        Victime saved = victimeRepository.save(adv);

        // Après changement : si tous les adversaires du sinistre sont TRANCHEE,
        // basculer le sinistre vers ATTENTE_PIECES_DE_RECLAMATION + positionRc TRANCHEE
        synchroniserSinistre(saved.getSinistre(), loginAuteur);

        return DataResponse.success(messageAction(r.action()), victimeMapper.toResponse(saved));
    }

    private void proposer(Victime adv, ActionRcRequest r, LocalDateTime now) {
        if (adv.getPositionRc() != PositionRc.EN_ATTENTE && adv.getPositionRc() != PositionRc.REJETEE) {
            throw new BadRequestException("Action PROPOSER autorisée uniquement depuis EN_ATTENTE ou REJETEE.");
        }
        if (r.pourcentage() == null) throw new BadRequestException("Pourcentage obligatoire pour PROPOSER.");
        adv.setPositionRc(PositionRc.EN_NEGOCIATION);
        adv.setPourcentageRcPropose(r.pourcentage());
        adv.setMotifRejetRc(null);
        adv.setNombreToursRc(nvl(adv.getNombreToursRc()) + 1);
        adv.setDateDerniereActionRc(now);
    }

    private void rejeter(Victime adv, ActionRcRequest r, LocalDateTime now) {
        if (adv.getPositionRc() != PositionRc.EN_NEGOCIATION) {
            throw new BadRequestException("Action REJETER autorisée uniquement depuis EN_NEGOCIATION.");
        }
        if (r.motifRejet() == null || r.motifRejet().trim().length() < 10) {
            throw new BadRequestException("Motif du rejet obligatoire (10 caractères minimum).");
        }
        adv.setPositionRc(PositionRc.REJETEE);
        adv.setMotifRejetRc(r.motifRejet().trim());
        adv.setDateDerniereActionRc(now);
    }

    private void accepter(Victime adv, LocalDateTime now) {
        if (adv.getPositionRc() != PositionRc.EN_NEGOCIATION) {
            throw new BadRequestException("Action ACCEPTER autorisée uniquement depuis EN_NEGOCIATION.");
        }
        if (adv.getPourcentageRcPropose() == null) {
            throw new BadRequestException("Aucun pourcentage en cours à accepter.");
        }
        adv.setPositionRc(PositionRc.TRANCHEE);
        adv.setPourcentageRcFinal(adv.getPourcentageRcPropose());
        adv.setDateDerniereActionRc(now);
    }

    private void trancher(Victime adv, ActionRcRequest r, LocalDateTime now) {
        if (adv.getPositionRc() != PositionRc.EN_NEGOCIATION && adv.getPositionRc() != PositionRc.REJETEE) {
            throw new BadRequestException("Action TRANCHER autorisée uniquement depuis EN_NEGOCIATION ou REJETEE.");
        }
        if (r.pourcentage() == null) throw new BadRequestException("Pourcentage obligatoire pour TRANCHER.");
        adv.setPositionRc(PositionRc.TRANCHEE);
        adv.setPourcentageRcFinal(r.pourcentage());
        adv.setDateDerniereActionRc(now);
    }

    /**
     * Synchronisation du sinistre : maintient sinistre.positionRc comme agrégat
     * des adversaires, et bascule le statut en ATTENTE_PIECES_DE_RECLAMATION
     * dès que tous les adversaires sont TRANCHEE.
     */
    private void synchroniserSinistre(Sinistre sinistre, String loginAuteur) {
        if (sinistre == null) return;
        List<Victime> adversaires = victimeRepository.findAllBySinistre(sinistre.getSinistreTrackingId())
                .stream().filter(Victime::isEstAdversaire).toList();
        if (adversaires.isEmpty()) return;

        // Agrégat de la position RC globale du dossier
        PositionRc agregee = agregerPositionRc(adversaires);
        sinistre.setPositionRc(agregee);

        // Auto-transition du statut : tous TRANCHEE → ATTENTE_PIECES_DE_RECLAMATION
        boolean tousTranchees = adversaires.stream().allMatch(a -> a.getPositionRc() == PositionRc.TRANCHEE);
        if (tousTranchees
                && (sinistre.getStatut() == StatutSinistre.ATTENTE_RC
                 || sinistre.getStatut() == StatutSinistre.ATTENTE_PV)) {
            sinistre.setStatut(StatutSinistre.ATTENTE_PIECES_DE_RECLAMATION);
            log.info("[WORKFLOW] Sinistre {} → ATTENTE_PIECES_DE_RECLAMATION (tous adversaires TRANCHEE)",
                    sinistre.getSinistreTrackingId());
        }
        sinistre.setUpdatedBy(loginAuteur);
        sinistreRepository.save(sinistre);
    }

    private PositionRc agregerPositionRc(List<Victime> adversaires) {
        boolean tousTranchees    = adversaires.stream().allMatch(a -> a.getPositionRc() == PositionRc.TRANCHEE);
        if (tousTranchees) return PositionRc.TRANCHEE;
        boolean auMoinsUnRejete  = adversaires.stream().anyMatch(a -> a.getPositionRc() == PositionRc.REJETEE);
        if (auMoinsUnRejete) return PositionRc.REJETEE;
        boolean auMoinsUnNego    = adversaires.stream().anyMatch(a -> a.getPositionRc() == PositionRc.EN_NEGOCIATION);
        if (auMoinsUnNego) return PositionRc.EN_NEGOCIATION;
        return PositionRc.EN_ATTENTE;
    }

    private static int nvl(Integer v) { return v == null ? 0 : v; }

    private static String messageAction(ActionRcRequest.Action a) {
        return switch (a) {
            case PROPOSER -> "Proposition RC enregistree";
            case REJETER  -> "Rejet RC enregistre";
            case ACCEPTER -> "Position RC tranchee (acceptation)";
            case TRANCHER -> "Position RC tranchee";
        };
    }
}
