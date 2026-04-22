package com.ossanasur.cbconnect.module.auth.service.impl;

import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.module.auth.dto.request.HabilitationRequest;
import com.ossanasur.cbconnect.module.auth.dto.response.HabilitationResponse;
import com.ossanasur.cbconnect.module.auth.entity.ModuleEntity;
import com.ossanasur.cbconnect.module.auth.mapper.HabilitationMapper;
import com.ossanasur.cbconnect.module.auth.repository.ModuleEntityRepository;
import com.ossanasur.cbconnect.module.auth.service.HabilitationService;
import com.ossanasur.cbconnect.security.entity.Habilitation;
import com.ossanasur.cbconnect.security.repository.HabilitationRepository;
import com.ossanasur.cbconnect.utils.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HabilitationServiceImpl implements HabilitationService {

    private final HabilitationRepository habilitationRepository;
    private final ModuleEntityRepository moduleRepository;
    private final HabilitationMapper mapper;

    @Override
    @Transactional
    public DataResponse<HabilitationResponse> create(HabilitationRequest r, String loginAuteur) {
        if (habilitationRepository.findActiveByCode(r.codeHabilitation()).isPresent()) {
            throw new IllegalArgumentException("Code habilitation existe deja : " + r.codeHabilitation());
        }
        ModuleEntity mod = moduleRepository.findActiveByTrackingId(r.moduleTrackingId())
            .orElseThrow(() -> new RessourceNotFoundException("Module introuvable : " + r.moduleTrackingId()));

        Habilitation h = Habilitation.builder()
            .habilitationTrackingId(UUID.randomUUID())
            .codeHabilitation(r.codeHabilitation())
            .libelleHabilitation(r.libelleHabilitation())
            .description(r.description())
            .action(r.action())
            .typeAcces(r.typeAcces())
            .moduleEntity(mod)
            .createdBy(loginAuteur)
            .activeData(true)
            .deletedData(false)
            .build();
        return DataResponse.created("Habilitation creee",
            mapper.toResponse(habilitationRepository.save(h)));
    }

    @Override
    @Transactional
    public DataResponse<HabilitationResponse> update(UUID id, HabilitationRequest r, String loginAuteur) {
        Habilitation existing = habilitationRepository.findActiveByTrackingId(id)
            .orElseThrow(() -> new RessourceNotFoundException("Habilitation introuvable"));
        ModuleEntity mod = moduleRepository.findActiveByTrackingId(r.moduleTrackingId())
            .orElseThrow(() -> new RessourceNotFoundException("Module introuvable : " + r.moduleTrackingId()));

        existing.setLibelleHabilitation(r.libelleHabilitation());
        existing.setDescription(r.description());
        existing.setAction(r.action());
        existing.setTypeAcces(r.typeAcces());
        existing.setModuleEntity(mod);
        existing.setUpdatedBy(loginAuteur);
        existing.setUpdatedAt(LocalDateTime.now());
        return DataResponse.success("Habilitation mise a jour",
            mapper.toResponse(habilitationRepository.save(existing)));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<HabilitationResponse> getByTrackingId(UUID id) {
        Habilitation h = habilitationRepository.findActiveByTrackingId(id)
            .orElseThrow(() -> new RessourceNotFoundException("Habilitation introuvable"));
        return DataResponse.success(mapper.toResponse(h));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<HabilitationResponse>> getAll() {
        List<HabilitationResponse> list = habilitationRepository.findAllActive()
            .stream().map(mapper::toResponse).collect(Collectors.toList());
        return DataResponse.success(list);
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<HabilitationResponse>> getByModule(UUID moduleTrackingId) {
        List<HabilitationResponse> list = habilitationRepository.findActiveByModule(moduleTrackingId)
            .stream().map(mapper::toResponse).collect(Collectors.toList());
        return DataResponse.success(list);
    }

    @Override
    @Transactional
    public DataResponse<Void> delete(UUID id, String loginAuteur) {
        Habilitation h = habilitationRepository.findActiveByTrackingId(id)
            .orElseThrow(() -> new RessourceNotFoundException("Habilitation introuvable"));
        h.setActiveData(false);
        h.setDeletedData(true);
        h.setDeletedBy(loginAuteur);
        h.setDeletedAt(LocalDateTime.now());
        habilitationRepository.save(h);
        return DataResponse.success("Habilitation supprimee", null);
    }
}
