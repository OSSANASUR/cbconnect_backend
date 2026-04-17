package com.ossanasur.cbconnect.module.pv.service.impl;
import com.ossanasur.cbconnect.common.enums.TypeTable;
import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.historique.PvSinistreVersioningService;
import com.ossanasur.cbconnect.module.auth.repository.UtilisateurRepository;
import com.ossanasur.cbconnect.module.pv.dto.request.PvSinistreRequest;
import com.ossanasur.cbconnect.module.pv.dto.response.PvSinistreResponse;
import com.ossanasur.cbconnect.module.pv.entity.PvSinistre;
import com.ossanasur.cbconnect.module.pv.mapper.PvSinistreMapper;
import com.ossanasur.cbconnect.module.pv.repository.PvSinistreRepository;
import com.ossanasur.cbconnect.module.pv.service.PvSinistreService;
import com.ossanasur.cbconnect.module.sinistre.repository.EntiteConstatRepository;
import com.ossanasur.cbconnect.module.sinistre.repository.SinistreRepository;
import com.ossanasur.cbconnect.utils.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@Service @RequiredArgsConstructor
public class PvSinistreServiceImpl implements PvSinistreService {
    private final PvSinistreRepository pvRepository;
    private final SinistreRepository sinistreRepository;
    private final EntiteConstatRepository entiteConstatRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PvSinistreVersioningService versioningService;
    private final PvSinistreMapper pvMapper;
    @Override @Transactional
    public DataResponse<PvSinistreResponse> enregistrer(PvSinistreRequest r, String loginAuteur) {
        var entite = entiteConstatRepository.findActiveByTrackingId(r.entiteConstatTrackingId())
            .orElseThrow(() -> new RessourceNotFoundException("Entite constat introuvable"));
        var enregistrePar = utilisateurRepository.findByEmailAndActiveDataTrueAndDeletedDataFalse(loginAuteur)
            .orElseThrow(() -> new RessourceNotFoundException("Utilisateur introuvable"));
        PvSinistre pv = PvSinistre.builder()
            .pvTrackingId(UUID.randomUUID()).numeroPv(r.numeroPv())
            .sensCirculation(r.sensCirculation()).lieuAccident(r.lieuAccident())
            .dateAccidentPv(r.dateAccidentPv()).dateReceptionBncb(r.dateReceptionBncb())
            .provenance(r.provenance()).referenceSinistreLiee(r.referenceSinistreLiee())
            .aCirconstances(r.aCirconstances() != null ? r.aCirconstances() : "NEANT")
            .aAuditions(r.aAuditions() != null ? r.aAuditions() : "NEANT")
            .aCroquis(r.aCroquis() != null ? r.aCroquis() : "NEANT")
            .remarques(r.remarques()).entiteConstat(entite).enregistrePar(enregistrePar)
            .createdBy(loginAuteur).activeData(true).deletedData(false).fromTable(TypeTable.PV_SINISTRE)
            .build();
        if (r.sinistreTrackingId() != null)
            sinistreRepository.findActiveByTrackingId(r.sinistreTrackingId()).ifPresent(pv::setSinistre);
        return DataResponse.created("PV enregistre", pvMapper.toResponse(pvRepository.save(pv)));
    }
    @Override @Transactional
    public DataResponse<PvSinistreResponse> modifier(UUID id, PvSinistreRequest r, String loginAuteur) {
        return DataResponse.success("PV modifie", pvMapper.toResponse(versioningService.createVersion(id, r, loginAuteur)));
    }
    @Override @Transactional(readOnly=true)
    public DataResponse<PvSinistreResponse> getByTrackingId(UUID id) {
        return DataResponse.success(pvMapper.toResponse(versioningService.getActiveVersion(id)));
    }
    @Override @Transactional(readOnly=true)
    public DataResponse<List<PvSinistreResponse>> getBySinistre(UUID sinistreId) {
        return DataResponse.success(pvRepository.findBySinistre(sinistreId).stream().map(pvMapper::toResponse).collect(Collectors.toList()));
    }
    @Override @Transactional(readOnly=true)
    public DataResponse<List<PvSinistreResponse>> getNonAssocies() {
        return DataResponse.success(pvRepository.findNonAssocies().stream().map(pvMapper::toResponse).collect(Collectors.toList()));
    }
    @Override @Transactional
    public DataResponse<Void> associerSinistre(UUID pvId, UUID sinistreId, String loginAuteur) {
        PvSinistre pv = versioningService.getActiveVersion(pvId);
        var sinistre = sinistreRepository.findActiveByTrackingId(sinistreId)
            .orElseThrow(() -> new RessourceNotFoundException("Sinistre introuvable"));
        pv.setSinistre(sinistre); pv.setUpdatedBy(loginAuteur); pvRepository.save(pv);
        return DataResponse.success("PV associe au sinistre", null);
    }
    @Override @Transactional
    public DataResponse<Void> supprimer(UUID id, String loginAuteur) {
        versioningService.softDelete(id, loginAuteur);
        return DataResponse.success("PV supprime", null);
    }
}
