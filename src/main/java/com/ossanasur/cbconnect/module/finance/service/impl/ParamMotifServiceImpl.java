package com.ossanasur.cbconnect.module.finance.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ossanasur.cbconnect.common.enums.TypeMotif;
import com.ossanasur.cbconnect.common.enums.TypeTable;
import com.ossanasur.cbconnect.exception.BadRequestException;
import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.module.finance.dto.request.ParamMotifRequest;
import com.ossanasur.cbconnect.module.finance.dto.response.ParamMotifResponse;
import com.ossanasur.cbconnect.module.finance.entity.ParamMotif;
import com.ossanasur.cbconnect.module.finance.repository.ParamMotifRepository;
import com.ossanasur.cbconnect.module.finance.service.ParamMotifService;
import com.ossanasur.cbconnect.utils.DataResponse;
import com.ossanasur.cbconnect.utils.PaginatedResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParamMotifServiceImpl implements ParamMotifService {

    private final ParamMotifRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<ParamMotifResponse> listerParType(TypeMotif type) {
        return repository.findActiveByType(type == null ? null : type.name()).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ParamMotifResponse> listerTous(int page, int size) {
        return PaginatedResponse.fromPage(
                repository.findAllActive(PageRequest.of(page, size)).map(this::toResponse),
                "Motifs");
    }

    @Override
    @Transactional(readOnly = true)
    public ParamMotifResponse getByTrackingId(UUID trackingId) {
        return toResponse(findActiveOrThrow(trackingId));
    }

    @Override
    @Transactional
    public DataResponse<ParamMotifResponse> creer(ParamMotifRequest request, String login) {
        boolean doublon = repository.existsByLibelleMotifAndTypeAndActiveDataTrueAndDeletedDataFalse(
                request.libelle(), request.type());
        if (doublon) {
            throw new BadRequestException(
                    "Un motif \"" + request.libelle() + "\" de type " + request.type() + " existe déjà.");
        }
        ParamMotif entite = ParamMotif.builder()
                .paramMotifTrackingId(UUID.randomUUID())
                .libelleMotif(request.libelle())
                .type(request.type())
                .actif(request.actif() == null ? Boolean.TRUE : request.actif())
                .activeData(true)
                .deletedData(false)
                .createdBy(login)
                .fromTable(TypeTable.PARAM_MOTIF) // ajouter dans l'enum TypeTable si absent
                .build();
        return DataResponse.created("Motif créé", toResponse(repository.save(entite)));
    }

    @Override
    @Transactional
    public DataResponse<ParamMotifResponse> modifier(UUID trackingId, ParamMotifRequest request, String login) {
        ParamMotif entite = findActiveOrThrow(trackingId);
        boolean doublon = repository
                .existsByLibelleMotifAndTypeAndActiveDataTrueAndDeletedDataFalseAndParamMotifTrackingIdNot(
                        request.libelle(), request.type(), trackingId);
        if (doublon) {
            throw new BadRequestException(
                    "Un autre motif \"" + request.libelle() + "\" de type " + request.type() + " existe déjà.");
        }
        entite.setLibelleMotif(request.libelle());
        entite.setType(request.type());
        if (request.actif() != null)
            entite.setActif(request.actif());
        entite.setUpdatedBy(login);
        entite.setUpdatedAt(LocalDateTime.now());
        return DataResponse.success("Motif modifié", toResponse(repository.save(entite)));
    }

    @Override
    @Transactional
    public DataResponse<Void> supprimer(UUID trackingId, String login) {
        ParamMotif entite = findActiveOrThrow(trackingId);
        entite.setActif(false);
        entite.setDeletedData(true);
        entite.setDeletedAt(LocalDateTime.now());
        entite.setDeletedBy(login);
        repository.save(entite);
        return DataResponse.success("Motif supprimé", null);
    }

    /**
     * Résout un motif par son libellé et son type — retourne le libellé pour snapshot.
     * Lance RessourceNotFoundException si aucun motif actif ne correspond.
     */
    @Transactional(readOnly = true)
    public String resolveLibelleByLibelleAndType(String libelle, TypeMotif type) {
        return repository.findActiveByLibelleAndType(libelle, type.name())
                .map(ParamMotif::getLibelleMotif)
                .orElseThrow(() -> new RessourceNotFoundException(
                        "Motif paramétré introuvable : '" + libelle + "' (type=" + type + ")"));
    }

    /**
     * Utilisé par PaiementServiceImpl — résout le trackingId et retourne le libellé
     * pour snapshot.
     * Lance BadRequestException si le motif est inactif ou de mauvais type.
     */
    @Transactional(readOnly = true)
    public String resolveLibelleForSnapshot(UUID trackingId, TypeMotif typeAttendu) {
        ParamMotif motif = findActiveOrThrow(trackingId);
        if (!motif.getActif()) {
            throw new BadRequestException("Le motif sélectionné est désactivé.");
        }
        if (motif.getType() != typeAttendu) {
            throw new BadRequestException(
                    "Le motif \"" + motif.getLibelleMotif() + "\" est de type " + motif.getType() +
                            " — type attendu : " + typeAttendu);
        }
        return motif.getLibelleMotif();
    }

    private ParamMotif findActiveOrThrow(UUID id) {
        return repository.findActiveByTrackingId(id)
                .orElseThrow(() -> new RessourceNotFoundException("Motif introuvable : " + id));
    }

    private ParamMotifResponse toResponse(ParamMotif m) {
        return new ParamMotifResponse(
                m.getParamMotifTrackingId(),
                m.getLibelleMotif(),
                m.getType(),
                Boolean.TRUE.equals(m.getActif()),
                m.getCreatedAt());
    }
}
