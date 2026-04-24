package com.ossanasur.cbconnect.module.finance.service.impl;

import com.ossanasur.cbconnect.common.enums.*;
import com.ossanasur.cbconnect.exception.*;
import com.ossanasur.cbconnect.module.auth.repository.OrganismeRepository;
import com.ossanasur.cbconnect.module.finance.dto.request.EncaissementRequest;
import com.ossanasur.cbconnect.module.finance.dto.response.EncaissementResponse;
import com.ossanasur.cbconnect.module.finance.entity.Encaissement;
import com.ossanasur.cbconnect.module.finance.mapper.EncaissementMapper;
import com.ossanasur.cbconnect.module.finance.repository.EncaissementRepository;
import com.ossanasur.cbconnect.module.finance.service.EncaissementService;
import com.ossanasur.cbconnect.module.sinistre.repository.SinistreRepository;
import com.ossanasur.cbconnect.utils.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EncaissementServiceImpl implements EncaissementService {
    private final EncaissementRepository encaissementRepository;
    private final SinistreRepository sinistreRepository;
    private final OrganismeRepository organismeRepository;
    private final EncaissementMapper mapper;

    @Override
    @Transactional
    public DataResponse<EncaissementResponse> create(EncaissementRequest r, String loginAuteur) {

        var sinistre = sinistreRepository.findActiveByTrackingId(r.sinistreTrackingId())
                .orElseThrow(() -> new RessourceNotFoundException("Sinistre introuvable"));

        var organisme = organismeRepository.findActiveByTrackingId(r.organismeEmetteurTrackingId())
                .orElseThrow(() -> new RessourceNotFoundException("Organisme emetteur introuvable"));

        // Calcul automatique frais de gestion (5% si sinistre SURVENU_TOGO)
        BigDecimal fraisGestion = BigDecimal.ZERO;
        if (TypeSinistre.SURVENU_TOGO.equals(sinistre.getTypeSinistre())) {
            fraisGestion = r.montantCheque().multiply(new BigDecimal("0.05")).setScale(2,
                    java.math.RoundingMode.HALF_UP);
        }

        Encaissement e = Encaissement.builder()
                .encaissementTrackingId(UUID.randomUUID()).numeroCheque(r.numeroCheque())
                .montantCheque(r.montantCheque()).montantTheorique(r.montantTheorique())
                .produitFraisGestion(fraisGestion).dateEmission(r.dateEmission())
                .dateReception(r.dateReception()).banqueEmettrice(r.banqueEmettrice())
                .statutCheque(StatutCheque.RECU).organismeEmetteur(organisme).sinistre(sinistre)
                .createdBy(loginAuteur).activeData(true).deletedData(false).fromTable(TypeTable.ENCAISSEMENT)
                .build();

        return DataResponse.created("Encaissement enregistre", mapper.toResponse(encaissementRepository.save(e)));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<EncaissementResponse> getByTrackingId(UUID id) {
        return DataResponse.success(mapper.toResponse(encaissementRepository.findActiveByTrackingId(id)
                .orElseThrow(() -> new RessourceNotFoundException("Encaissement introuvable"))));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<EncaissementResponse>> getBySinistre(UUID sinistreId) {
        return DataResponse.success(encaissementRepository.findBySinistre(sinistreId).stream().map(mapper::toResponse)
                .collect(Collectors.toList()));
    }

    @Override
    @Transactional
    public DataResponse<Void> encaisser(UUID id, LocalDate dateEncaissement, String loginAuteur) {
        Encaissement e = encaissementRepository.findActiveByTrackingId(id)
                .orElseThrow(() -> new RessourceNotFoundException("Encaissement introuvable"));
        e.setStatutCheque(StatutCheque.ENCAISSE);
        e.setDateEncaissement(dateEncaissement);
        e.setUpdatedBy(loginAuteur);
        encaissementRepository.save(e);
        return DataResponse.success("Cheque encaisse", null);
    }

    @Override
    @Transactional
    public DataResponse<Void> annuler(UUID id, String motif, String loginAuteur) {
        Encaissement e = encaissementRepository.findActiveByTrackingId(id)
                .orElseThrow(() -> new RessourceNotFoundException("Encaissement introuvable"));
        e.setStatutCheque(StatutCheque.ANNULE);
        e.setMotifAnnulation(motif);
        e.setUpdatedBy(loginAuteur);
        encaissementRepository.save(e);
        return DataResponse.success("Encaissement annule", null);
    }
}
