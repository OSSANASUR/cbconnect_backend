package com.ossanasur.cbconnect.module.auth.service.impl;

import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.historique.ProfilVersioningService;
import com.ossanasur.cbconnect.module.auth.dto.request.ProfilRequest;
import com.ossanasur.cbconnect.module.auth.dto.response.ProfilResponse;
import com.ossanasur.cbconnect.module.auth.entity.Organisme;
import com.ossanasur.cbconnect.module.auth.entity.Profil;
import com.ossanasur.cbconnect.module.auth.mapper.ProfilMapper;
import com.ossanasur.cbconnect.module.auth.repository.OrganismeRepository;
import com.ossanasur.cbconnect.module.auth.repository.ProfilRepository;
import com.ossanasur.cbconnect.module.auth.repository.UtilisateurRepository;
import com.ossanasur.cbconnect.module.auth.service.ProfilService;
import com.ossanasur.cbconnect.security.entity.Habilitation;
import com.ossanasur.cbconnect.security.repository.HabilitationRepository;
import com.ossanasur.cbconnect.utils.DataResponse;
import com.ossanasur.cbconnect.utils.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfilServiceImpl implements ProfilService {

    private final ProfilRepository profilRepository;
    private final OrganismeRepository organismeRepository;
    private final HabilitationRepository habilitationRepository;
    private final ProfilMapper profilMapper;
    private final ProfilVersioningService versioningService;
    private final UtilisateurRepository utilisateurRepository;

    @Override
    @Transactional
    public DataResponse<ProfilResponse> create(ProfilRequest r, String loginAuteur) {
        Organisme organisme;
        if (r.organismeTrackingId() != null) {
            organisme = organismeRepository.findActiveByTrackingId(r.organismeTrackingId())
                    .orElseThrow(() -> new RessourceNotFoundException("Organisme introuvable"));
        } else {
            organisme = utilisateurRepository.findByEmailOrUsername(loginAuteur, loginAuteur)
                    .map(u -> u.getProfil() != null ? u.getProfil().getOrganisme() : null)
                    .orElse(null);
        }

        List<Habilitation> habilitations = r.habilitationTrackingIds() == null ? Collections.emptyList() :
                r.habilitationTrackingIds().stream()
                        .map(hid -> habilitationRepository.findActiveByTrackingId(hid)
                                .orElseThrow(() -> new RessourceNotFoundException("Habilitation introuvable : " + hid)))
                        .collect(Collectors.toList());

        Profil p = Profil.builder()
                .profilTrackingId(UUID.randomUUID())
                .profilNom(r.profilNom())
                .commentaire(r.commentaire())
                .organisme(organisme)
                .habilitations(habilitations)
                .createdBy(loginAuteur)
                .activeData(true)
                .deletedData(false)
                .build();
        return DataResponse.created("Profil cree avec succes",
                profilMapper.toResponse(profilRepository.save(p)));
    }

    @Override
    @Transactional
    public DataResponse<ProfilResponse> update(UUID id, ProfilRequest r, String loginAuteur) {
        Profil updated = versioningService.createVersion(id, r, loginAuteur);
        return DataResponse.success("Profil mis a jour avec succes", profilMapper.toResponse(updated));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<ProfilResponse> getByTrackingId(UUID id) {
        Profil p = versioningService.getActiveVersion(id);
        return DataResponse.success(profilMapper.toResponse(p));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<ProfilResponse>> getAll() {
        List<ProfilResponse> list = profilRepository.findAllActive()
                .stream().map(profilMapper::toResponse).collect(Collectors.toList());
        return DataResponse.success(list);
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<ProfilResponse>> getByOrganisme(UUID organismeTrackingId) {
        List<ProfilResponse> list = profilRepository.findAllActiveByOrganisme(organismeTrackingId)
                .stream().map(profilMapper::toResponse).collect(Collectors.toList());
        return DataResponse.success(list);
    }

    @Override
    @Transactional
    public DataResponse<Void> delete(UUID id, String loginAuteur) {
        versioningService.softDelete(id, loginAuteur);
        return DataResponse.success("Profil supprime avec succes", null);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ProfilResponse> getHistory(UUID id, int page, int size) {
        Page<ProfilResponse> history = profilRepository
                .findHistoryByTrackingId(id, PageRequest.of(page, size))
                .map(profilMapper::toResponse);
        return PaginatedResponse.fromPage(history, "Historique profil");
    }
}
