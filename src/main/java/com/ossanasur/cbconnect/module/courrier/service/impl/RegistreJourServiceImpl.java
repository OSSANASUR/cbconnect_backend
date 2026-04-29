package com.ossanasur.cbconnect.module.courrier.service.impl;

import com.ossanasur.cbconnect.common.enums.StatutRegistre;
import com.ossanasur.cbconnect.common.enums.TypeRegistre;
import com.ossanasur.cbconnect.common.enums.TypeTable;
import com.ossanasur.cbconnect.exception.AlreadyExistException;
import com.ossanasur.cbconnect.exception.BadRequestException;
import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.module.auth.repository.UtilisateurRepository;
import com.ossanasur.cbconnect.module.courrier.dto.request.RegistreJourRequest;
import com.ossanasur.cbconnect.module.courrier.dto.request.VisaRegistreRequest;
import com.ossanasur.cbconnect.module.courrier.dto.response.CourrierResponse;
import com.ossanasur.cbconnect.module.courrier.dto.response.RegistreJourResponse;
import com.ossanasur.cbconnect.module.courrier.entity.RegistreJour;
import com.ossanasur.cbconnect.module.courrier.mapper.CourrierMapper;
import com.ossanasur.cbconnect.module.courrier.mapper.RegistreJourMapper;
import com.ossanasur.cbconnect.module.courrier.repository.CourrierRepository;
import com.ossanasur.cbconnect.module.courrier.repository.RegistreJourRepository;
import com.ossanasur.cbconnect.module.courrier.service.RegistreJourService;
import com.ossanasur.cbconnect.utils.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistreJourServiceImpl implements RegistreJourService {

    private final RegistreJourRepository registreRepository;
    private final CourrierRepository courrierRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final RegistreJourMapper mapper;
    private final CourrierMapper courrierMapper;

    @Override
    @Transactional
    public DataResponse<RegistreJourResponse> ouvrir(RegistreJourRequest r, String loginAuteur) {
        registreRepository.findByDateAndType(r.dateJour(), r.typeRegistre()).ifPresent(existing -> {
            throw new AlreadyExistException(
                "Un registre " + r.typeRegistre() + " du " + r.dateJour() + " existe déjà");
        });

        var secretaire = utilisateurRepository.findActiveByUsername(loginAuteur)
            .orElse(null);

        RegistreJour rj = RegistreJour.builder()
            .registreTrackingId(UUID.randomUUID())
            .dateJour(r.dateJour())
            .typeRegistre(r.typeRegistre())
            .statut(StatutRegistre.OUVERT)
            .secretaire(secretaire)
            .createdBy(loginAuteur)
            .activeData(true)
            .deletedData(false)
            .fromTable(TypeTable.REGISTRE_JOUR)
            .build();

        return DataResponse.created("Registre ouvert", mapper.toResponse(registreRepository.save(rj)));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<RegistreJourResponse> getByTrackingId(UUID id) {
        RegistreJour rj = registreRepository.findActiveByTrackingId(id)
            .orElseThrow(() -> new RessourceNotFoundException("Registre introuvable"));
        return DataResponse.success(mapper.toResponse(rj));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<RegistreJourResponse> getDuJour(LocalDate date, TypeRegistre type) {
        return registreRepository.findByDateAndType(date, type)
            .map(rj -> DataResponse.success(mapper.toResponse(rj)))
            .orElseGet(() -> DataResponse.success(null));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<RegistreJourResponse>> getAll() {
        return DataResponse.success(
            registreRepository.findAllActive().stream().map(mapper::toResponse).toList());
    }

    @Override
    @Transactional
    public DataResponse<RegistreJourResponse> cloturer(UUID id, String loginAuteur) {
        RegistreJour rj = registreRepository.findActiveByTrackingId(id)
            .orElseThrow(() -> new RessourceNotFoundException("Registre introuvable"));
        if (rj.getStatut() != StatutRegistre.OUVERT)
            throw new BadRequestException("Seul un registre OUVERT peut être clôturé");

        rj.setStatut(StatutRegistre.CLOS);
        rj.setDateCloture(LocalDateTime.now());
        rj.setClosPar(loginAuteur);
        rj.setUpdatedBy(loginAuteur);
        return DataResponse.success("Registre clôturé — prêt pour visa",
            mapper.toResponse(registreRepository.save(rj)));
    }

    @Override
    @Transactional
    public DataResponse<RegistreJourResponse> viser(UUID id, VisaRegistreRequest r, String loginAuteur) {
        RegistreJour rj = registreRepository.findActiveByTrackingId(id)
            .orElseThrow(() -> new RessourceNotFoundException("Registre introuvable"));
        if (rj.getStatut() != StatutRegistre.CLOS)
            throw new BadRequestException("Le registre doit être CLOS pour être visé");

        var chef = utilisateurRepository.findActiveByUsername(loginAuteur)
            .orElse(null);

        rj.setStatut(StatutRegistre.VISE);
        rj.setViseParUtilisateur(chef);
        rj.setDateVisa(LocalDateTime.now());
        rj.setCommentaireChef(r.commentaireChef());
        if (r.scanGedDocumentId() != null) rj.setScanGedDocumentId(r.scanGedDocumentId());
        rj.setUpdatedBy(loginAuteur);

        return DataResponse.success("Registre visé", mapper.toResponse(registreRepository.save(rj)));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<CourrierResponse>> getCourriers(UUID id) {
        List<CourrierResponse> list = courrierRepository.findByRegistre(id)
            .stream().map(courrierMapper::toResponse).toList();
        return DataResponse.success(list);
    }

    @Override
    @Transactional
    public RegistreJour getOuCreerRegistreOuvert(LocalDate date, TypeRegistre type, String loginAuteur) {
        return registreRepository.findByDateAndType(date, type).orElseGet(() -> {
            var secretaire = utilisateurRepository
                .findActiveByUsername(loginAuteur).orElse(null);
            RegistreJour rj = RegistreJour.builder()
                .registreTrackingId(UUID.randomUUID())
                .dateJour(date).typeRegistre(type)
                .statut(StatutRegistre.OUVERT)
                .secretaire(secretaire)
                .createdBy(loginAuteur)
                .activeData(true).deletedData(false)
                .fromTable(TypeTable.REGISTRE_JOUR).build();
            return registreRepository.save(rj);
        });
    }
}
