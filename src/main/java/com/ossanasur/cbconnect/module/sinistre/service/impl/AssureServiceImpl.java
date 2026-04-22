package com.ossanasur.cbconnect.module.sinistre.service.impl;

import com.ossanasur.cbconnect.common.enums.TypeTable;
import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.module.auth.repository.OrganismeRepository;
import com.ossanasur.cbconnect.module.sinistre.dto.request.AssureRequest;
import com.ossanasur.cbconnect.module.sinistre.dto.response.AssureResponse;
import com.ossanasur.cbconnect.module.sinistre.entity.Assure;
import com.ossanasur.cbconnect.module.sinistre.mapper.AssureMapper;
import com.ossanasur.cbconnect.module.sinistre.repository.AssureRepository;
import com.ossanasur.cbconnect.module.sinistre.service.AssureService;
import com.ossanasur.cbconnect.utils.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssureServiceImpl implements AssureService {

    private final AssureRepository assureRepository;
    private final OrganismeRepository organismeRepository;
    private final AssureMapper assureMapper;

    @Override
    @Transactional
    public DataResponse<AssureResponse> create(AssureRequest r, String loginAuteur) {
        Assure a = Assure.builder()
                .assureTrackingId(UUID.randomUUID())
                .nomAssure(r.nomAssure())
                .prenomAssure(r.prenomAssure())
                .nomComplet(r.nomComplet())
                .numeroPolice(r.numeroPolice())
                .numeroAttestation(r.numeroAttestation())
                .numeroCGrise(r.numeroCGrise())
                .proprietaireVehicule(r.proprietaireVehicule())
                .immatriculation(r.immatriculation())
                .marqueVehicule(r.marqueVehicule())
                .telephone(r.telephone())
                .adresse(r.adresse())
                .estPersonneMorale(Boolean.TRUE.equals(r.estPersonneMorale()))
                .profession(r.profession())
                .prochaineVT(r.prochaineVT())
                .capaciteVehicule(r.capaciteVehicule())
                .nbPersonnesABord(r.nbPersonnesABord())
                .aRemorque(Boolean.TRUE.equals(r.aRemorque()))
                .createdBy(loginAuteur)
                .activeData(true)
                .deletedData(false)
                .fromTable(TypeTable.ASSURE)
                .build();

        if (r.organismeTrackingId() != null) {
            organismeRepository.findActiveByTrackingId(r.organismeTrackingId())
                    .ifPresent(a::setOrganisme);
        }

        return DataResponse.created("Assure enregistre", assureMapper.toResponse(assureRepository.save(a)));
    }

    @Override
    @Transactional
    public DataResponse<AssureResponse> update(UUID id, AssureRequest r, String loginAuteur) {
        Assure a = assureRepository.findActiveByTrackingId(id)
                .orElseThrow(() -> new RessourceNotFoundException("Assure introuvable"));
        a.setNomAssure(r.nomAssure());
        a.setPrenomAssure(r.prenomAssure());
        a.setNomComplet(r.nomComplet());
        a.setNumeroPolice(r.numeroPolice());
        a.setNumeroAttestation(r.numeroAttestation());
        a.setNumeroCGrise(r.numeroCGrise());
        a.setProprietaireVehicule(r.proprietaireVehicule());
        a.setImmatriculation(r.immatriculation());
        a.setMarqueVehicule(r.marqueVehicule());
        a.setTelephone(r.telephone());
        a.setAdresse(r.adresse());
        if (r.estPersonneMorale() != null) a.setEstPersonneMorale(r.estPersonneMorale());
        a.setProfession(r.profession());
        a.setProchaineVT(r.prochaineVT());
        a.setCapaciteVehicule(r.capaciteVehicule());
        a.setNbPersonnesABord(r.nbPersonnesABord());
        if (r.aRemorque() != null) a.setARemorque(r.aRemorque());
        a.setUpdatedBy(loginAuteur);

        if (r.organismeTrackingId() != null) {
            organismeRepository.findActiveByTrackingId(r.organismeTrackingId())
                    .ifPresent(a::setOrganisme);
        } else {
            a.setOrganisme(null);
        }

        return DataResponse.success("Assure mis a jour", assureMapper.toResponse(assureRepository.save(a)));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<AssureResponse> getByTrackingId(UUID id) {
        Assure a = assureRepository.findActiveByTrackingId(id)
                .orElseThrow(() -> new RessourceNotFoundException("Assure introuvable"));
        return DataResponse.success(assureMapper.toResponse(a));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<AssureResponse> getByNumeroAttestation(String numero) {
        Assure a = assureRepository.findByNumeroAttestation(numero)
                .orElseThrow(() -> new RessourceNotFoundException("Aucun assure pour l'attestation " + numero));
        return DataResponse.success(assureMapper.toResponse(a));
    }

    @Override
    @Transactional
    public DataResponse<Void> delete(UUID id, String loginAuteur) {
        Assure a = assureRepository.findActiveByTrackingId(id)
                .orElseThrow(() -> new RessourceNotFoundException("Assure introuvable"));
        a.setDeletedData(true);
        a.setActiveData(false);
        a.setUpdatedBy(loginAuteur);
        assureRepository.save(a);
        return DataResponse.success("Assure supprime", null);
    }
}
