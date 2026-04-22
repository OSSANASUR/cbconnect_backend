package com.ossanasur.cbconnect.module.auth.service.impl;

import com.ossanasur.cbconnect.exception.AlreadyExistException;
import com.ossanasur.cbconnect.historique.BanqueVersioningService;
import com.ossanasur.cbconnect.module.auth.dto.request.BanqueRequest;
import com.ossanasur.cbconnect.module.auth.dto.response.BanqueResponse;
import com.ossanasur.cbconnect.module.auth.entity.Banque;
import com.ossanasur.cbconnect.module.auth.mapper.BanqueMapper;
import com.ossanasur.cbconnect.module.auth.repository.BanqueRepository;
import com.ossanasur.cbconnect.module.auth.service.BanqueService;
import com.ossanasur.cbconnect.utils.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BanqueServiceImpl implements BanqueService {

    private final BanqueRepository banqueRepository;
    private final BanqueMapper banqueMapper;
    private final BanqueVersioningService versioningService;

    @Override
    @Transactional
    public DataResponse<BanqueResponse> create(BanqueRequest r, String loginAuteur) {
        String code = r.code().toUpperCase();
        if (banqueRepository.existsByCodeAndActiveDataTrueAndDeletedDataFalse(code))
            throw new AlreadyExistException("Une banque avec le code '" + code + "' existe déjà");

        Banque b = Banque.builder()
                .banqueTrackingId(UUID.randomUUID())
                .nom(r.nom())
                .code(code)
                .codeBic(r.codeBic() != null ? r.codeBic().toUpperCase() : null)
                .agence(r.agence())
                .ville(r.ville())
                .codePays(r.codePays() != null ? r.codePays().toUpperCase() : null)
                .telephone(r.telephone())
                .createdBy(loginAuteur)
                .activeData(true)
                .deletedData(false)
                .build();

        return DataResponse.created("Banque créée avec succès",
                banqueMapper.toResponse(banqueRepository.save(b)));
    }

    @Override
    @Transactional
    public DataResponse<BanqueResponse> update(UUID id, BanqueRequest r, String loginAuteur) {
        Banque updated = versioningService.createVersion(id, r, loginAuteur);
        return DataResponse.success("Banque mise à jour avec succès", banqueMapper.toResponse(updated));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<BanqueResponse> getByTrackingId(UUID id) {
        Banque b = versioningService.getActiveVersion(id);
        return DataResponse.success(banqueMapper.toResponse(b));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<BanqueResponse>> getAll() {
        List<BanqueResponse> list = banqueRepository.findAllActive()
                .stream().map(banqueMapper::toResponse).collect(Collectors.toList());
        return DataResponse.success(list);
    }

    @Override
    @Transactional
    public DataResponse<Void> delete(UUID id, String loginAuteur) {
        versioningService.softDelete(id, loginAuteur);
        return DataResponse.success("Banque supprimée avec succès", null);
    }
}
