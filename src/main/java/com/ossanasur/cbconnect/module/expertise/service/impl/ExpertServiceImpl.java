package com.ossanasur.cbconnect.module.expertise.service.impl;

import com.ossanasur.cbconnect.common.enums.*;
import com.ossanasur.cbconnect.historique.ExpertVersioningService;
import com.ossanasur.cbconnect.module.expertise.dto.request.ExpertRequest;
import com.ossanasur.cbconnect.module.expertise.dto.response.ExpertResponse;
import com.ossanasur.cbconnect.module.expertise.entity.Expert;
import com.ossanasur.cbconnect.module.expertise.mapper.ExpertMapper;
import com.ossanasur.cbconnect.module.expertise.repository.ExpertRepository;
import com.ossanasur.cbconnect.module.expertise.service.ExpertService;
import com.ossanasur.cbconnect.module.pays.repository.PaysRepository;
import com.ossanasur.cbconnect.utils.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpertServiceImpl implements ExpertService {
    private final ExpertRepository expertRepository;
    private final PaysRepository paysRepository;
    private final ExpertVersioningService versioningService;
    private final ExpertMapper expertMapper;

    @Override
    @Transactional
    public DataResponse<ExpertResponse> create(ExpertRequest r, String loginAuteur) {
        Expert e = Expert.builder().expertTrackingId(UUID.randomUUID())
                .typeExpert(r.typeExpert()).nomComplet(r.nomComplet())
                .specialite(r.specialite()).nif(r.nif()).tauxRetenue(r.tauxRetenue()).actif(r.actif())
                .createdBy(loginAuteur).activeData(true).deletedData(false).fromTable(TypeTable.EXPERT)
                .build();
        if (r.paysTrackingId() != null)
            paysRepository.findActiveByTrackingId(r.paysTrackingId()).ifPresent(e::setPays);
        return DataResponse.created("Expert cree", expertMapper.toResponse(expertRepository.save(e)));
    }

    @Override
    @Transactional
    public DataResponse<ExpertResponse> update(UUID id, ExpertRequest r, String loginAuteur) {
        return DataResponse.success("Expert mis a jour",
                expertMapper.toResponse(versioningService.createVersion(id, r, loginAuteur)));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<ExpertResponse> getByTrackingId(UUID id) {
        return DataResponse.success(expertMapper.toResponse(versioningService.getActiveVersion(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<ExpertResponse>> getAllActifsByType(TypeExpert type) {
        return DataResponse.success(expertRepository.findAllActifsByType(type).stream().map(expertMapper::toResponse)
                .collect(Collectors.toList()));
    }

    @Override
    @Transactional
    public DataResponse<Void> delete(UUID id, String loginAuteur) {
        versioningService.softDelete(id, loginAuteur);
        return DataResponse.success("Expert supprime", null);
    }
}
