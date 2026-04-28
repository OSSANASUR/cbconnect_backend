package com.ossanasur.cbconnect.module.baremes.service.impl;

import com.ossanasur.cbconnect.module.baremes.dto.request.*;
import com.ossanasur.cbconnect.module.baremes.dto.response.*;
import com.ossanasur.cbconnect.module.baremes.entity.*;
import com.ossanasur.cbconnect.module.baremes.mapper.BaremesMapper;
import com.ossanasur.cbconnect.module.baremes.repository.*;
import com.ossanasur.cbconnect.module.baremes.service.BaremesService;
import com.ossanasur.cbconnect.utils.DataResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BaremesServiceImpl implements BaremesService {

    private final BaremeCapitalisationRepository capitalisationRepo;
    private final BaremeValeurPointIpRepository valeurPointIpRepo;
    private final BaremeCleRepartition265Repository cleRepartitionRepo;
    private final BaremePrejudiceMoral266Repository prejudiceMoralRepo;
    private final BaremePretiumDolorisRepository pretiumDolorisRepo;
    private final BaremesMapper mapper;

    // ─── Capitalisation ──────────────────────────────────────────────
    @Override
    public DataResponse<List<BaremeCapitalisationResponse>> listCapitalisation() {
        return DataResponse.success(capitalisationRepo.findAllOrdered().stream().map(mapper::toResponse).toList());
    }

    @Override
    public DataResponse<BaremeCapitalisationResponse> createCapitalisation(BaremeCapitalisationRequest r) {
        BaremeCapitalisation b = BaremeCapitalisation.builder()
                .typeBareme(r.typeBareme()).age(r.age())
                .prixFrancRente(r.prixFrancRente()).tauxCapitalisation(r.tauxCapitalisation())
                .tableMortalite(r.tableMortalite()).ageLimitePaiement(r.ageLimitePaiement())
                .actif(r.actif() == null || r.actif()).build();
        return DataResponse.created("Barème créé", mapper.toResponse(capitalisationRepo.save(b)));
    }

    @Override
    public DataResponse<BaremeCapitalisationResponse> updateCapitalisation(Integer id, BaremeCapitalisationRequest r) {
        BaremeCapitalisation b = capitalisationRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Barème capitalisation introuvable: " + id));
        b.setTypeBareme(r.typeBareme());
        b.setAge(r.age());
        b.setPrixFrancRente(r.prixFrancRente());
        b.setTauxCapitalisation(r.tauxCapitalisation());
        b.setTableMortalite(r.tableMortalite());
        b.setAgeLimitePaiement(r.ageLimitePaiement());
        if (r.actif() != null) b.setActif(r.actif());
        return DataResponse.success("Barème modifié", mapper.toResponse(capitalisationRepo.save(b)));
    }

    @Override
    public DataResponse<Void> deleteCapitalisation(Integer id) {
        if (!capitalisationRepo.existsById(id)) throw new EntityNotFoundException("Barème capitalisation introuvable: " + id);
        capitalisationRepo.deleteById(id);
        return DataResponse.success("Barème supprimé", null);
    }

    // ─── Valeur point IP ─────────────────────────────────────────────
    @Override
    public DataResponse<List<BaremeValeurPointIpResponse>> listValeurPointIp() {
        return DataResponse.success(valeurPointIpRepo.findAllOrdered().stream().map(mapper::toResponse).toList());
    }

    @Override
    public DataResponse<BaremeValeurPointIpResponse> createValeurPointIp(BaremeValeurPointIpRequest r) {
        if (r.ippMax().compareTo(r.ippMin()) < 0)
            throw new IllegalArgumentException("ippMax doit être >= ippMin");
        BaremeValeurPointIp b = BaremeValeurPointIp.builder()
                .ageMin(r.ageMin()).ageMax(r.ageMax())
                .ippMin(r.ippMin()).ippMax(r.ippMax())
                .valeurPoint(r.valeurPoint())
                .actif(r.actif() == null || r.actif()).build();
        return DataResponse.created("Valeur point IP créée", mapper.toResponse(valeurPointIpRepo.save(b)));
    }

    @Override
    public DataResponse<BaremeValeurPointIpResponse> updateValeurPointIp(Integer id, BaremeValeurPointIpRequest r) {
        BaremeValeurPointIp b = valeurPointIpRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Barème valeur point IP introuvable: " + id));
        if (r.ippMax().compareTo(r.ippMin()) < 0)
            throw new IllegalArgumentException("ippMax doit être >= ippMin");
        b.setAgeMin(r.ageMin());
        b.setAgeMax(r.ageMax());
        b.setIppMin(r.ippMin());
        b.setIppMax(r.ippMax());
        b.setValeurPoint(r.valeurPoint());
        if (r.actif() != null) b.setActif(r.actif());
        return DataResponse.success("Valeur point IP modifiée", mapper.toResponse(valeurPointIpRepo.save(b)));
    }

    @Override
    public DataResponse<Void> deleteValeurPointIp(Integer id) {
        if (!valeurPointIpRepo.existsById(id)) throw new EntityNotFoundException("Valeur point IP introuvable: " + id);
        valeurPointIpRepo.deleteById(id);
        return DataResponse.success("Valeur point IP supprimée", null);
    }

    // ─── Clé répartition 265 ─────────────────────────────────────────
    @Override
    public DataResponse<List<BaremeCleRepartition265Response>> listCleRepartition() {
        return DataResponse.success(cleRepartitionRepo.findAll().stream().map(mapper::toResponse).toList());
    }

    @Override
    public DataResponse<BaremeCleRepartition265Response> createCleRepartition(BaremeCleRepartition265Request r) {
        if (cleRepartitionRepo.findByCodeSituation(r.codeSituation()).isPresent())
            throw new IllegalArgumentException("Code situation déjà utilisé : " + r.codeSituation());
        BaremeCleRepartition265 b = BaremeCleRepartition265.builder()
                .codeSituation(r.codeSituation()).libelleSituation(r.libelleSituation())
                .conditionConjoint(Boolean.TRUE.equals(r.conditionConjoint()))
                .conditionEnfant(Boolean.TRUE.equals(r.conditionEnfant()))
                .nombreMaxEnfants(r.nombreMaxEnfants())
                .cleAscendants(orZero(r.cleAscendants())).cleConjoints(orZero(r.cleConjoints()))
                .cleEnfants(orZero(r.cleEnfants())).cleOrphelinsDoubles(orZero(r.cleOrphelinsDoubles()))
                .actif(r.actif() == null || r.actif()).build();
        return DataResponse.created("Clé répartition créée", mapper.toResponse(cleRepartitionRepo.save(b)));
    }

    @Override
    public DataResponse<BaremeCleRepartition265Response> updateCleRepartition(Integer id, BaremeCleRepartition265Request r) {
        BaremeCleRepartition265 b = cleRepartitionRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Clé répartition introuvable: " + id));
        b.setCodeSituation(r.codeSituation());
        b.setLibelleSituation(r.libelleSituation());
        b.setConditionConjoint(Boolean.TRUE.equals(r.conditionConjoint()));
        b.setConditionEnfant(Boolean.TRUE.equals(r.conditionEnfant()));
        b.setNombreMaxEnfants(r.nombreMaxEnfants());
        b.setCleAscendants(orZero(r.cleAscendants()));
        b.setCleConjoints(orZero(r.cleConjoints()));
        b.setCleEnfants(orZero(r.cleEnfants()));
        b.setCleOrphelinsDoubles(orZero(r.cleOrphelinsDoubles()));
        if (r.actif() != null) b.setActif(r.actif());
        return DataResponse.success("Clé répartition modifiée", mapper.toResponse(cleRepartitionRepo.save(b)));
    }

    @Override
    public DataResponse<Void> deleteCleRepartition(Integer id) {
        if (!cleRepartitionRepo.existsById(id)) throw new EntityNotFoundException("Clé répartition introuvable: " + id);
        cleRepartitionRepo.deleteById(id);
        return DataResponse.success("Clé répartition supprimée", null);
    }

    // ─── Préjudice moral 266 ────────────────────────────────────────
    @Override
    public DataResponse<List<BaremePrejudiceMoral266Response>> listPrejudiceMoral() {
        return DataResponse.success(prejudiceMoralRepo.findAll().stream().map(mapper::toResponse).toList());
    }

    @Override
    public DataResponse<BaremePrejudiceMoral266Response> createPrejudiceMoral(BaremePrejudiceMoral266Request r) {
        if (prejudiceMoralRepo.findByLienParente(r.lienParente()).isPresent())
            throw new IllegalArgumentException("Lien parenté déjà défini : " + r.lienParente());
        BaremePrejudiceMoral266 b = BaremePrejudiceMoral266.builder()
                .lienParente(r.lienParente()).cle(r.cle())
                .plafondCategorie(r.plafondCategorie())
                .actif(r.actif() == null || r.actif()).build();
        return DataResponse.created("Préjudice moral créé", mapper.toResponse(prejudiceMoralRepo.save(b)));
    }

    @Override
    public DataResponse<BaremePrejudiceMoral266Response> updatePrejudiceMoral(Integer id, BaremePrejudiceMoral266Request r) {
        BaremePrejudiceMoral266 b = prejudiceMoralRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Préjudice moral introuvable: " + id));
        b.setLienParente(r.lienParente());
        b.setCle(r.cle());
        b.setPlafondCategorie(r.plafondCategorie());
        if (r.actif() != null) b.setActif(r.actif());
        return DataResponse.success("Préjudice moral modifié", mapper.toResponse(prejudiceMoralRepo.save(b)));
    }

    @Override
    public DataResponse<Void> deletePrejudiceMoral(Integer id) {
        if (!prejudiceMoralRepo.existsById(id)) throw new EntityNotFoundException("Préjudice moral introuvable: " + id);
        prejudiceMoralRepo.deleteById(id);
        return DataResponse.success("Préjudice moral supprimé", null);
    }

    // ─── Pretium doloris / préjudice esthétique (art. 262) ──────────
    @Override
    public DataResponse<List<BaremePretiumDolorisResponse>> listPretiumDoloris() {
        return DataResponse.success(pretiumDolorisRepo.findAllOrdered().stream().map(mapper::toResponse).toList());
    }

    @Override
    public DataResponse<BaremePretiumDolorisResponse> createPretiumDoloris(BaremePretiumDolorisRequest r) {
        if (pretiumDolorisRepo.findByQualification(r.qualification()).isPresent())
            throw new IllegalArgumentException("Qualification déjà définie : " + r.qualification());
        BaremePretiumDoloris b = BaremePretiumDoloris.builder()
                .qualification(r.qualification()).points(r.points())
                .moral(Boolean.TRUE.equals(r.moral()))
                .actif(r.actif() == null || r.actif()).build();
        return DataResponse.created("Pretium doloris créé", mapper.toResponse(pretiumDolorisRepo.save(b)));
    }

    @Override
    public DataResponse<BaremePretiumDolorisResponse> updatePretiumDoloris(Integer id, BaremePretiumDolorisRequest r) {
        BaremePretiumDoloris b = pretiumDolorisRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pretium doloris introuvable: " + id));
        b.setQualification(r.qualification());
        b.setPoints(r.points());
        b.setMoral(Boolean.TRUE.equals(r.moral()));
        if (r.actif() != null) b.setActif(r.actif());
        return DataResponse.success("Pretium doloris modifié", mapper.toResponse(pretiumDolorisRepo.save(b)));
    }

    @Override
    public DataResponse<Void> deletePretiumDoloris(Integer id) {
        if (!pretiumDolorisRepo.existsById(id)) throw new EntityNotFoundException("Pretium doloris introuvable: " + id);
        pretiumDolorisRepo.deleteById(id);
        return DataResponse.success("Pretium doloris supprimé", null);
    }

    private BigDecimal orZero(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
}
