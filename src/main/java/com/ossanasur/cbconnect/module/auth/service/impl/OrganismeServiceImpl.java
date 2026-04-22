package com.ossanasur.cbconnect.module.auth.service.impl;

import com.ossanasur.cbconnect.common.enums.TypeOrganisme;
import com.ossanasur.cbconnect.exception.AlreadyExistException;
import com.ossanasur.cbconnect.historique.OrganismeVersioningService;
import com.ossanasur.cbconnect.module.auth.dto.request.OrganismeRequest;
import com.ossanasur.cbconnect.module.auth.dto.response.OrganismeResponse;
import com.ossanasur.cbconnect.module.auth.entity.Organisme;
import com.ossanasur.cbconnect.module.auth.mapper.OrganismeMapper;
import com.ossanasur.cbconnect.module.auth.repository.OrganismeRepository;
import com.ossanasur.cbconnect.module.auth.service.OrganismeService;
import com.ossanasur.cbconnect.security.dto.response.TwoFactorStatusResponse;
import com.ossanasur.cbconnect.utils.DataResponse;
import com.ossanasur.cbconnect.utils.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganismeServiceImpl implements OrganismeService {

    private final OrganismeRepository organismeRepository;
    private final OrganismeMapper organismeMapper;
    private final OrganismeVersioningService versioningService;

    @Override
    @Transactional
    public DataResponse<OrganismeResponse> create(OrganismeRequest r, String loginAuteur) {
        if (organismeRepository.existsByCodeAndActiveDataTrueAndDeletedDataFalse(r.code()))
            throw new AlreadyExistException("Un organisme avec le code '" + r.code() + "' existe deja");
        if (organismeRepository.existsByEmailAndActiveDataTrueAndDeletedDataFalse(r.email()))
            throw new AlreadyExistException("Un organisme avec l'email '" + r.email() + "' existe deja");

        Organisme o = Organisme.builder()
                .organismeTrackingId(UUID.randomUUID())
                .typeOrganisme(r.typeOrganisme()).raisonSociale(r.raisonSociale())
                .code(r.code()).email(r.email()).responsable(r.responsable())
                .contacts(r.contacts()).codePays(r.codePays()).codePaysBCB(r.codePaysBCB())
                .paysId(r.paysId()).dateCreation(r.dateCreation())
                .numeroAgrement(r.numeroAgrement()).apiEndpointUrl(r.apiEndpointUrl())
                .adresse(r.adresse()).boitePostale(r.boitePostale()).ville(r.ville())
                .telephonePrincipal(r.telephonePrincipal()).fax(r.fax()).siteWeb(r.siteWeb())
                .active(r.active()).createdBy(loginAuteur).activeData(true).deletedData(false)
                .fromTable(com.ossanasur.cbconnect.common.enums.TypeTable.ORGANISME)
                .build();
        return DataResponse.created("Organisme cree avec succes", organismeMapper.toResponse(organismeRepository.save(o)));
    }

    @Override
    @Transactional
    public DataResponse<OrganismeResponse> update(UUID id, OrganismeRequest r, String loginAuteur) {
        Organisme updated = versioningService.createVersion(id, r, loginAuteur);
        return DataResponse.success("Organisme mis a jour avec succes", organismeMapper.toResponse(updated));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<OrganismeResponse> getByTrackingId(UUID id) {
        Organisme o = versioningService.getActiveVersion(id);
        return DataResponse.success(organismeMapper.toResponse(o));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<OrganismeResponse>> getAll() {
        List<OrganismeResponse> list = organismeRepository.findAllActive()
                .stream().map(organismeMapper::toResponse).collect(Collectors.toList());
        return DataResponse.success(list);
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<OrganismeResponse>> getAllByType(TypeOrganisme type) {
        List<OrganismeResponse> list = organismeRepository.findAllActiveByType(type)
                .stream().map(organismeMapper::toResponse).collect(Collectors.toList());
        return DataResponse.success(list);
    }

    @Override
    @Transactional
    public DataResponse<Void> delete(UUID id, String loginAuteur) {
        versioningService.softDelete(id, loginAuteur);
        return DataResponse.success("Organisme supprime avec succes", null);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<OrganismeResponse> getHistory(UUID id, int page, int size) {
        Page<OrganismeResponse> history = organismeRepository
                .findHistoryByTrackingId(id, PageRequest.of(page, size))
                .map(organismeMapper::toResponse);
        return PaginatedResponse.fromPage(history, "Historique organisme");
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<TwoFactorStatusResponse> getTwoFactor(UUID trackingId) {
        Organisme o = versioningService.getActiveVersion(trackingId);
        return DataResponse.success(new TwoFactorStatusResponse(o.isTwoFactorEnabled()));
    }

    @Override
    @Transactional
    public DataResponse<TwoFactorStatusResponse> updateTwoFactor(UUID trackingId, boolean enabled, String loginAuteur) {
        Organisme o = versioningService.getActiveVersion(trackingId);
        o.setTwoFactorEnabled(enabled);
        o.setUpdatedBy(loginAuteur);
        o.setUpdatedAt(java.time.LocalDateTime.now());
        organismeRepository.save(o);
        String msg = enabled
                ? "Double authentification activée pour l'organisme"
                : "Double authentification désactivée pour l'organisme";
        return DataResponse.success(msg, new TwoFactorStatusResponse(enabled));
    }
}
