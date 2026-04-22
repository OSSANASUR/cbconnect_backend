package com.ossanasur.cbconnect.module.sinistre.service.impl;
import com.ossanasur.cbconnect.common.enums.TypeEntiteConstat;
import com.ossanasur.cbconnect.common.enums.TypeTable;
import com.ossanasur.cbconnect.exception.AlreadyExistException;
import com.ossanasur.cbconnect.historique.EntiteConstatVersioningService;
import com.ossanasur.cbconnect.module.sinistre.dto.request.EntiteConstatRequest;
import com.ossanasur.cbconnect.module.sinistre.dto.response.EntiteConstatResponse;
import com.ossanasur.cbconnect.module.sinistre.entity.EntiteConstat;
import com.ossanasur.cbconnect.module.sinistre.mapper.EntiteConstatMapper;
import com.ossanasur.cbconnect.module.sinistre.repository.EntiteConstatRepository;
import com.ossanasur.cbconnect.module.sinistre.service.EntiteConstatService;
import com.ossanasur.cbconnect.utils.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class EntiteConstatServiceImpl implements EntiteConstatService {
    private final EntiteConstatRepository repository;
    private final EntiteConstatVersioningService versioningService;
    private final EntiteConstatMapper mapper;

    @Override @Transactional
    public DataResponse<EntiteConstatResponse> create(EntiteConstatRequest r, String loginAuteur) {
        if (repository.existsByNomActive(r.nom()))
            throw new AlreadyExistException("Une entite de constat avec ce nom existe deja : " + r.nom());
        EntiteConstat e = EntiteConstat.builder()
            .entiteConstatTrackingId(UUID.randomUUID())
            .nom(r.nom()).type(r.type())
            .localite(r.localite()).codePostal(r.codePostal())
            .actif(r.actif() != null ? r.actif() : true)
            .createdBy(loginAuteur).activeData(true).deletedData(false)
            .fromTable(TypeTable.ENTITE_CONSTAT)
            .build();
        return DataResponse.created("Entite de constat creee", mapper.toResponse(repository.save(e)));
    }

    @Override @Transactional
    public DataResponse<EntiteConstatResponse> update(UUID id, EntiteConstatRequest r, String loginAuteur) {
        EntiteConstat updated = versioningService.createVersion(id, r, loginAuteur);
        return DataResponse.success("Entite de constat mise a jour", mapper.toResponse(updated));
    }

    @Override @Transactional(readOnly = true)
    public DataResponse<EntiteConstatResponse> getByTrackingId(UUID id) {
        return DataResponse.success(mapper.toResponse(versioningService.getActiveVersion(id)));
    }

    @Override @Transactional(readOnly = true)
    public DataResponse<List<EntiteConstatResponse>> getAll(boolean actifsOnly) {
        List<EntiteConstat> list = actifsOnly ? repository.findAllActifs() : repository.findAllActive();
        return DataResponse.success(list.stream().map(mapper::toResponse).collect(Collectors.toList()));
    }

    @Override @Transactional(readOnly = true)
    public DataResponse<List<EntiteConstatResponse>> getByType(TypeEntiteConstat type) {
        return DataResponse.success(repository.findAllByType(type).stream()
            .map(mapper::toResponse).collect(Collectors.toList()));
    }

    @Override @Transactional
    public DataResponse<Void> delete(UUID id, String loginAuteur) {
        versioningService.softDelete(id, loginAuteur);
        return DataResponse.success("Entite de constat supprimee", null);
    }
}
