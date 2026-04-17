package com.ossanasur.cbconnect.module.sinistre.service.impl;
import com.ossanasur.cbconnect.common.enums.*;
import com.ossanasur.cbconnect.exception.*;
import com.ossanasur.cbconnect.historique.VictimeVersioningService;
import com.ossanasur.cbconnect.module.pays.repository.PaysRepository;
import com.ossanasur.cbconnect.module.sinistre.dto.request.VictimeRequest;
import com.ossanasur.cbconnect.module.sinistre.dto.response.VictimeResponse;
import com.ossanasur.cbconnect.module.sinistre.entity.Victime;
import com.ossanasur.cbconnect.module.sinistre.mapper.VictimeMapper;
import com.ossanasur.cbconnect.module.sinistre.repository.*;
import com.ossanasur.cbconnect.module.sinistre.service.VictimeService;
import com.ossanasur.cbconnect.utils.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@Service @RequiredArgsConstructor
public class VictimeServiceImpl implements VictimeService {
    private final VictimeRepository victimeRepository;
    private final SinistreRepository sinistreRepository;
    private final PaysRepository paysRepository;
    private final VictimeVersioningService versioningService;
    private final VictimeMapper victimeMapper;

    @Override @Transactional
    public DataResponse<VictimeResponse> create(VictimeRequest r, String loginAuteur) {
        var sinistre = sinistreRepository.findActiveByTrackingId(r.sinistreTrackingId())
            .orElseThrow(()->new RessourceNotFoundException("Sinistre introuvable"));
        Victime v = Victime.builder()
            .victimeTrackingId(UUID.randomUUID()).nom(r.nom()).prenoms(r.prenoms())
            .dateNaissance(r.dateNaissance()).sexe(r.sexe()).nationalite(r.nationalite())
            .statutActivite(r.statutActivite())
            .revenuMensuel(r.revenuMensuel()!=null?r.revenuMensuel():java.math.BigDecimal.ZERO)
            .typeVictime(TypeVictime.NEUTRE).statutVictime(StatutVictime.NEUTRE)
            .sinistre(sinistre)
            .createdBy(loginAuteur).activeData(true).deletedData(false).fromTable(TypeTable.VICTIME)
            .build();
        if(r.paysResidenceTrackingId()!=null)
            paysRepository.findActiveByTrackingId(r.paysResidenceTrackingId()).ifPresent(v::setPaysResidence);
        return DataResponse.created("Victime enregistree", victimeMapper.toResponse(victimeRepository.save(v)));
    }

    @Override @Transactional
    public DataResponse<VictimeResponse> update(UUID id, VictimeRequest r, String loginAuteur) {
        return DataResponse.success("Victime mise a jour", victimeMapper.toResponse(versioningService.createVersion(id, r, loginAuteur)));
    }

    @Override @Transactional(readOnly=true)
    public DataResponse<VictimeResponse> getByTrackingId(UUID id) {
        return DataResponse.success(victimeMapper.toResponse(versioningService.getActiveVersion(id)));
    }

    @Override @Transactional(readOnly=true)
    public DataResponse<List<VictimeResponse>> getBySinistre(UUID sinistreId) {
        return DataResponse.success(victimeRepository.findAllBySinistre(sinistreId).stream()
            .map(victimeMapper::toResponse).collect(Collectors.toList()));
    }

    @Override @Transactional
    public DataResponse<Void> delete(UUID id, String loginAuteur) {
        versioningService.softDelete(id, loginAuteur);
        return DataResponse.success("Victime supprimee", null);
    }

    @Override @Transactional
    public DataResponse<Void> changerStatutVictime(UUID id, String statut, String loginAuteur) {
        Victime v = versioningService.getActiveVersion(id);
        try { v.setStatutVictime(StatutVictime.valueOf(statut.toUpperCase())); }
        catch(IllegalArgumentException e) { throw new BadRequestException("Statut victime invalide: " + statut); }
        v.setUpdatedBy(loginAuteur); victimeRepository.save(v);
        return DataResponse.success("Statut victime mis a jour", null);
    }
}
