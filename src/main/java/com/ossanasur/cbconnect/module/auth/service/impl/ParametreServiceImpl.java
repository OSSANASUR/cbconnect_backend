package com.ossanasur.cbconnect.module.auth.service.impl;

import com.ossanasur.cbconnect.exception.AlreadyExistException;
import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.historique.ParametreVersioningService;
import com.ossanasur.cbconnect.module.auth.dto.request.ParametreRequest;
import com.ossanasur.cbconnect.module.auth.dto.response.ParametreResponse;
import com.ossanasur.cbconnect.module.auth.entity.Parametre;
import com.ossanasur.cbconnect.module.auth.mapper.ParametreMapper;
import com.ossanasur.cbconnect.module.auth.repository.ParametreRepository;
import com.ossanasur.cbconnect.module.auth.service.ParametreService;
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
public class ParametreServiceImpl implements ParametreService {

    private final ParametreRepository parametreRepository;
    private final ParametreMapper parametreMapper;
    private final ParametreVersioningService versioningService;

    @Override
    @Transactional
    public DataResponse<ParametreResponse> create(ParametreRequest r, String loginAuteur) {
        if (parametreRepository.existsByCleAndActiveDataTrueAndDeletedDataFalse(r.cle()))
            throw new AlreadyExistException("Un parametre avec la cle '" + r.cle() + "' existe deja");

        Parametre p = Parametre.builder()
                .parametreTrackingId(UUID.randomUUID())
                .typeParametre(r.typeParametre())
                .cle(r.cle())
                .valeur(r.valeur())
                .description(r.description())
                .createdBy(loginAuteur)
                .activeData(true)
                .deletedData(false)
                .build();
        return DataResponse.created("Parametre cree avec succes",
                parametreMapper.toResponse(parametreRepository.save(p)));
    }

    @Override
    @Transactional
    public DataResponse<ParametreResponse> update(UUID id, ParametreRequest r, String loginAuteur) {
        Parametre updated = versioningService.createVersion(id, r, loginAuteur);
        return DataResponse.success("Parametre mis a jour avec succes", parametreMapper.toResponse(updated));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<ParametreResponse> getByTrackingId(UUID id) {
        Parametre p = versioningService.getActiveVersion(id);
        return DataResponse.success(parametreMapper.toResponse(p));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<ParametreResponse> getByCle(String cle) {
        Parametre p = parametreRepository.findByCle(cle)
                .orElseThrow(() -> new RessourceNotFoundException("Parametre non trouve : " + cle));
        return DataResponse.success(parametreMapper.toResponse(p));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<ParametreResponse>> getAll() {
        List<ParametreResponse> list = parametreRepository.findAllActive()
                .stream().map(parametreMapper::toResponse).collect(Collectors.toList());
        return DataResponse.success(list);
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<ParametreResponse>> getByCategorie(String categorie) {
        List<ParametreResponse> list = parametreRepository.findByCategorie(categorie)
                .stream().map(parametreMapper::toResponse).collect(Collectors.toList());
        return DataResponse.success(list);
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<ParametreResponse>> getAllListe() {
        List<ParametreResponse> list = parametreRepository.findAllListe()
                .stream().map(parametreMapper::toResponse).collect(Collectors.toList());
        return DataResponse.success(list);
    }

    @Override
    @Transactional
    public DataResponse<Void> delete(UUID id, String loginAuteur) {
        versioningService.softDelete(id, loginAuteur);
        return DataResponse.success("Parametre supprime avec succes", null);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ParametreResponse> getHistory(UUID id, int page, int size) {
        Page<ParametreResponse> history = parametreRepository
                .findHistoryByTrackingId(id, PageRequest.of(page, size))
                .map(parametreMapper::toResponse);
        return PaginatedResponse.fromPage(history, "Historique parametre");
    }

    @Override
    @Transactional(readOnly = true)
    public String getValeur(String cle, String defaut) {
        return parametreRepository.findByCle(cle)
                .map(Parametre::getValeur)
                .orElse(defaut);
    }
}
