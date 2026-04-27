package com.ossanasur.cbconnect.module.expertise.service.impl;

import com.ossanasur.cbconnect.common.enums.*;
import com.ossanasur.cbconnect.historique.ExpertiseMedicaleVersioningService;
import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.module.expertise.dto.request.ExpertiseMedicaleRequest;
import com.ossanasur.cbconnect.module.expertise.dto.response.ExpertiseMedicaleResponse;
import com.ossanasur.cbconnect.module.expertise.entity.ExpertiseMedicale;
import com.ossanasur.cbconnect.module.expertise.mapper.ExpertiseMedicaleMapper;
import com.ossanasur.cbconnect.module.expertise.repository.*;
import com.ossanasur.cbconnect.module.expertise.service.ExpertiseMedicaleService;
import com.ossanasur.cbconnect.module.sinistre.repository.VictimeRepository;
import com.ossanasur.cbconnect.utils.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpertiseMedicaleServiceImpl implements ExpertiseMedicaleService {
    private final ExpertiseMedicaleRepository expertiseMedRepository;
    private final VictimeRepository victimeRepository;
    private final ExpertRepository expertRepository;
    private final ExpertiseMedicaleVersioningService versioningService;
    private final ExpertiseMedicaleMapper mapper;

    @Override
    @Transactional
    public DataResponse<ExpertiseMedicaleResponse> create(ExpertiseMedicaleRequest r, String loginAuteur) {
        var victime = victimeRepository.findActiveByTrackingId(r.victimeTrackingId())
                .orElseThrow(() -> new RessourceNotFoundException("Victime introuvable"));
        ExpertiseMedicale e = ExpertiseMedicale.builder()
                .expertiseMedTrackingId(UUID.randomUUID())
                .typeExpertise(r.typeExpertise()).dateDemande(r.dateDemande())
                .dateRapport(r.dateRapport()).dateConsolidation(r.dateConsolidation())
                .tauxIpp(r.tauxIpp() != null ? r.tauxIpp() : java.math.BigDecimal.ZERO)
                .dureeIttJours(r.dureeIttJours() != null ? r.dureeIttJours() : 0)
                .dureeItpJours(r.dureeItpJours() != null ? r.dureeItpJours() : 0)
                .pretiumDoloris(r.pretiumDoloris() != null ? r.pretiumDoloris() : QualificationPretium.NEANT)
                .prejudiceEsthetique(
                        r.prejudiceEsthetique() != null ? r.prejudiceEsthetique() : QualificationPretium.NEANT)
                .necessiteTiercePersonne(r.necessiteTiercePersonne() != null && r.necessiteTiercePersonne())
                .honoraires(r.honoraires() != null ? r.honoraires() : java.math.BigDecimal.ZERO)
                .honorairesContreExpertise(r.honorairesContreExpertise() != null ? r.honorairesContreExpertise()
                        : java.math.BigDecimal.ZERO)
                .ossanGedDocumentId(r.ossanGedDocumentId())
                .victime(victime).createdBy(loginAuteur).activeData(true).deletedData(false)
                .fromTable(TypeTable.EXPERTISE_MEDICALE).build();
        if (r.expertTrackingId() != null)
            expertRepository.findActiveByTrackingId(r.expertTrackingId()).ifPresent(e::setExpert);
        // Mise a jour statut victime
        victime.setTypeVictime(TypeVictime.BLESSE);
        victimeRepository.save(victime);
        return DataResponse.created("Expertise medicale creee", mapper.toResponse(expertiseMedRepository.save(e)));
    }

    @Override
    @Transactional
    public DataResponse<ExpertiseMedicaleResponse> update(UUID id, ExpertiseMedicaleRequest r, String loginAuteur) {
        return DataResponse.success("Expertise mise a jour",
                mapper.toResponse(versioningService.createVersion(id, r, loginAuteur)));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<ExpertiseMedicaleResponse> getByTrackingId(UUID id) {
        return DataResponse.success(mapper.toResponse(versioningService.getActiveVersion(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<ExpertiseMedicaleResponse>> getByVictime(UUID victimeId) {
        return DataResponse.success(expertiseMedRepository.findByVictime(victimeId).stream().map(mapper::toResponse)
                .collect(Collectors.toList()));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<ExpertiseMedicaleResponse>> getEnAttente() {
        return DataResponse.success(
                expertiseMedRepository.findSansRapport().stream().map(mapper::toResponse).collect(Collectors.toList()));
    }

    @Override
    @Transactional
    public DataResponse<Void> delete(UUID id, String loginAuteur) {
        versioningService.softDelete(id, loginAuteur);
        return DataResponse.success("Expertise supprimee", null);
    }
}