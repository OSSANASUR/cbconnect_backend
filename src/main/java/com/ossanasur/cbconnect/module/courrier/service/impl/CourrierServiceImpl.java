package com.ossanasur.cbconnect.module.courrier.service.impl;
import com.ossanasur.cbconnect.common.enums.*;
import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.module.courrier.dto.request.CourrierRequest;
import com.ossanasur.cbconnect.module.courrier.dto.response.CourrierResponse;
import com.ossanasur.cbconnect.module.courrier.entity.Courrier;
import com.ossanasur.cbconnect.module.courrier.mapper.CourrierMapper;
import com.ossanasur.cbconnect.module.courrier.repository.CourrierRepository;
import com.ossanasur.cbconnect.module.courrier.service.CourrierService;
import com.ossanasur.cbconnect.module.sinistre.repository.SinistreRepository;
import com.ossanasur.cbconnect.utils.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@Service @RequiredArgsConstructor
public class CourrierServiceImpl implements CourrierService {
    private final CourrierRepository courrierRepository;
    private final SinistreRepository sinistreRepository;
    private final CourrierMapper mapper;
    @Override @Transactional
    public DataResponse<CourrierResponse> enregistrer(CourrierRequest r, String loginAuteur) {
        // Auto-generation reference si non fournie
        String ref = r.referenceCourrier();
        if(ref == null || ref.isBlank()) {
            long seq = courrierRepository.count() + 1;
            String sens = TypeCourrier.SORTANT.equals(r.typeCourrier()) ? "LKS" : "ENT";
            ref = sens + "/" + java.time.LocalDate.now().getYear() + "/" + String.format("%05d", seq);
        }
        Courrier c = Courrier.builder()
            .courrierTrackingId(UUID.randomUUID()).referenceCourrier(ref)
            .typeCourrier(r.typeCourrier()).nature(r.nature())
            .expediteur(r.expediteur()).destinataire(r.destinataire()).objet(r.objet())
            .dateCourrier(r.dateCourrier()).dateReception(r.dateReception())
            .canal(r.canal()!=null?r.canal():CanalCourrier.MAIL).traite(false)
            .createdBy(loginAuteur).activeData(true).deletedData(false).fromTable(TypeTable.COURRIER).build();
        if(r.sinistreTrackingId()!=null)
            sinistreRepository.findActiveByTrackingId(r.sinistreTrackingId()).ifPresent(c::setSinistre);
        return DataResponse.created("Courrier enregistre : " + ref, mapper.toResponse(courrierRepository.save(c)));
    }
    @Override @Transactional(readOnly=true)
    public DataResponse<CourrierResponse> getByTrackingId(UUID id) {
        return DataResponse.success(mapper.toResponse(courrierRepository.findActiveByTrackingId(id)
            .orElseThrow(() -> new RessourceNotFoundException("Courrier introuvable"))));
    }
    @Override @Transactional(readOnly=true)
    public DataResponse<List<CourrierResponse>> getBySinistre(UUID sinistreId) {
        return DataResponse.success(courrierRepository.findBySinistre(sinistreId).stream().map(mapper::toResponse).collect(Collectors.toList()));
    }
    @Override @Transactional(readOnly=true)
    public DataResponse<List<CourrierResponse>> getNonTraites() {
        return DataResponse.success(courrierRepository.findNonTraites().stream().map(mapper::toResponse).collect(Collectors.toList()));
    }
    @Override @Transactional
    public DataResponse<Void> marquerTraite(UUID id, String loginAuteur) {
        Courrier c = courrierRepository.findActiveByTrackingId(id)
            .orElseThrow(() -> new RessourceNotFoundException("Courrier introuvable"));
        c.setTraite(true); c.setDateTraitement(LocalDateTime.now()); c.setUpdatedBy(loginAuteur);
        courrierRepository.save(c);
        return DataResponse.success("Courrier marque comme traite", null);
    }
    @Override @Transactional
    public DataResponse<Void> supprimer(UUID id, String loginAuteur) {
        Courrier c = courrierRepository.findActiveByTrackingId(id)
            .orElseThrow(() -> new RessourceNotFoundException("Courrier introuvable"));
        c.setActiveData(false); c.setDeletedData(true); c.setUpdatedBy(loginAuteur);
        courrierRepository.save(c);
        return DataResponse.success("Courrier supprime", null);
    }
}
