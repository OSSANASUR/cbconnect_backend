package com.ossanasur.cbconnect.module.indemnisation.service.impl;

import com.ossanasur.cbconnect.common.enums.TypeTable;
import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.module.auth.repository.UtilisateurRepository;
import com.ossanasur.cbconnect.module.indemnisation.dto.request.*;
import com.ossanasur.cbconnect.module.indemnisation.dto.response.*;
import com.ossanasur.cbconnect.module.indemnisation.entity.*;
import com.ossanasur.cbconnect.module.indemnisation.mapper.OffreIndemnisationMapper;
import com.ossanasur.cbconnect.module.indemnisation.repository.*;
import com.ossanasur.cbconnect.module.indemnisation.service.*;
import com.ossanasur.cbconnect.module.sinistre.repository.VictimeRepository;
import com.ossanasur.cbconnect.utils.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IndemnisationServiceImpl implements IndemnisationService {
    private final OffreIndemnisationRepository offreRepository;
    private final AyantDroitRepository ayantDroitRepository;
    private final VictimeRepository victimeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CalculCimaService calculCimaService;
    private final OffreIndemnisationMapper mapper;

    @Override
    @Transactional
    public DataResponse<OffreIndemnisationResponse> calculerOffreBlesse(UUID victimeId, String loginAuteur) {
        var victime = victimeRepository.findActiveByTrackingId(victimeId)
                .orElseThrow(() -> new RessourceNotFoundException("Victime introuvable"));
        OffreIndemnisation offre;
        if (com.ossanasur.cbconnect.common.enums.TypeVictime.DECEDE.equals(victime.getTypeVictime())) {
            offre = calculCimaService.calculerOffreDeces(victime, loginAuteur);
        } else {
            offre = calculCimaService.calculerOffreBlesse(victime, loginAuteur);
        }
        return DataResponse.created("Offre calculee : " + offre.getMontantTotalOffre() + " FCFA",
                mapper.toResponse(offre));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<OffreIndemnisationResponse> getOffreByVictime(UUID victimeId) {
        return DataResponse.success(mapper.toResponse(offreRepository.findLastByVictime(victimeId)
                .orElseThrow(() -> new RessourceNotFoundException("Aucune offre pour cette victime"))));
    }

    @Override
    @Transactional
    public DataResponse<OffreIndemnisationResponse> validerOffre(UUID offreId, String loginAuteur) {
        OffreIndemnisation offre = offreRepository.findActiveByTrackingId(offreId)
                .orElseThrow(() -> new RessourceNotFoundException("Offre introuvable"));
        var validateur = utilisateurRepository.findByEmailAndActiveDataTrueAndDeletedDataFalse(loginAuteur)
                .orElseThrow(() -> new RessourceNotFoundException("Validateur introuvable"));
        offre.setDateValidation(LocalDateTime.now());
        offre.setValidePar(validateur);
        offre.setUpdatedBy(loginAuteur);
        return DataResponse.success("Offre validee", mapper.toResponse(offreRepository.save(offre)));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<BigDecimal> calculerPenalites(UUID offreId) {
        OffreIndemnisation offre = offreRepository.findActiveByTrackingId(offreId)
                .orElseThrow(() -> new RessourceNotFoundException("Offre introuvable"));
        BigDecimal penalites = calculCimaService.calculerPenalitesRetard(offre);
        return DataResponse.success("Penalites calculees", penalites);
    }

    @Override
    @Transactional
    public DataResponse<AyantDroitResponse> ajouterAyantDroit(AyantDroitRequest r, String loginAuteur) {
        var victime = victimeRepository.findActiveByTrackingId(r.victimeTrackingId())
                .orElseThrow(() -> new RessourceNotFoundException("Victime introuvable"));
        AyantDroit a = AyantDroit.builder().ayantDroitTrackingId(UUID.randomUUID())
                .nom(r.nom()).prenoms(r.prenoms()).dateNaissance(r.dateNaissance()).sexe(r.sexe())
                .lien(r.lien()).estOrphelinDouble(r.estOrphelinDouble()).poursuiteEtudes(r.poursuiteEtudes())
                .montantPe(BigDecimal.ZERO).montantPm(BigDecimal.ZERO).montantTotal(BigDecimal.ZERO)
                .victime(victime).createdBy(loginAuteur).activeData(true).deletedData(false)
                .fromTable(TypeTable.AYANT_DROIT).build();
        return DataResponse.created("Ayant droit ajoute", mapper.toAyantDroitResponse(ayantDroitRepository.save(a)));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<AyantDroitResponse>> getAyantsDroitByVictime(UUID victimeId) {
        return DataResponse.success(ayantDroitRepository.findByVictime(victimeId).stream()
                .map(mapper::toAyantDroitResponse).collect(Collectors.toList()));
    }
}