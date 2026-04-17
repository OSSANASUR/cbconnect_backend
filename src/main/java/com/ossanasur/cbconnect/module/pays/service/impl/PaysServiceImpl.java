package com.ossanasur.cbconnect.module.pays.service.impl;
import com.ossanasur.cbconnect.common.enums.TypeTable;
import com.ossanasur.cbconnect.exception.AlreadyExistException;
import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.historique.PaysVersioningService;
import com.ossanasur.cbconnect.module.pays.dto.request.PaysRequest;
import com.ossanasur.cbconnect.module.pays.dto.response.PaysResponse;
import com.ossanasur.cbconnect.module.pays.entity.Pays;
import com.ossanasur.cbconnect.module.pays.mapper.PaysMapper;
import com.ossanasur.cbconnect.module.pays.repository.PaysRepository;
import com.ossanasur.cbconnect.module.pays.service.PaysService;
import com.ossanasur.cbconnect.utils.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@Service @RequiredArgsConstructor
public class PaysServiceImpl implements PaysService {
    private final PaysRepository paysRepository;
    private final PaysMapper paysMapper;
    private final PaysVersioningService versioningService;
    @Override @Transactional
    public DataResponse<PaysResponse> create(PaysRequest r, String loginAuteur) {
        if (paysRepository.findByCodeIso(r.codeIso()).isPresent())
            throw new AlreadyExistException("Pays avec codeIso '" + r.codeIso() + "' existe deja");
        Pays p = Pays.builder().paysTrackingId(UUID.randomUUID()).codeIso(r.codeIso())
            .codeCarteBrune(r.codeCarteBrune()).libelle(r.libelle()).smigMensuel(r.smigMensuel())
            .monnaie(r.monnaie()).tauxChangeXof(r.tauxChangeXof() != null ? r.tauxChangeXof() : java.math.BigDecimal.ONE)
            .ageRetraite(r.ageRetraite() != null ? r.ageRetraite() : 60).actif(r.actif())
            .createdBy(loginAuteur).activeData(true).deletedData(false).fromTable(TypeTable.PAYS).build();
        return DataResponse.created("Pays cree avec succes", paysMapper.toResponse(paysRepository.save(p)));
    }
    @Override @Transactional
    public DataResponse<PaysResponse> update(UUID id, PaysRequest r, String loginAuteur) {
        return DataResponse.success("Pays mis a jour", paysMapper.toResponse(versioningService.createVersion(id, r, loginAuteur)));
    }
    @Override @Transactional(readOnly=true)
    public DataResponse<PaysResponse> getByTrackingId(UUID id) {
        return DataResponse.success(paysMapper.toResponse(versioningService.getActiveVersion(id)));
    }
    @Override @Transactional(readOnly=true)
    public DataResponse<PaysResponse> getByCodeIso(String code) {
        return DataResponse.success(paysMapper.toResponse(
            paysRepository.findByCodeIso(code).orElseThrow(() -> new RessourceNotFoundException("Pays introuvable: " + code))));
    }
    @Override @Transactional(readOnly=true)
    public DataResponse<List<PaysResponse>> getAll() {
        return DataResponse.success(paysRepository.findAllActifs().stream().map(paysMapper::toResponse).collect(Collectors.toList()));
    }
    @Override @Transactional
    public DataResponse<Void> delete(UUID id, String loginAuteur) {
        versioningService.softDelete(id, loginAuteur);
        return DataResponse.success("Pays supprime", null);
    }
}
