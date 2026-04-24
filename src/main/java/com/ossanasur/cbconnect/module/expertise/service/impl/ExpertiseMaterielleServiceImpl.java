package com.ossanasur.cbconnect.module.expertise.service.impl;

import com.ossanasur.cbconnect.common.enums.TypeTable;
import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.module.expertise.dto.request.ExpertiseMaterielleRequest;
import com.ossanasur.cbconnect.module.expertise.dto.response.ExpertiseMaterielleResponse;
import com.ossanasur.cbconnect.module.expertise.entity.ExpertiseMaterielle;
import com.ossanasur.cbconnect.module.expertise.repository.ExpertRepository;
import com.ossanasur.cbconnect.module.expertise.repository.ExpertiseMaterielleRepository;
import com.ossanasur.cbconnect.module.sinistre.repository.SinistreRepository;
import com.ossanasur.cbconnect.module.sinistre.repository.VictimeRepository;
import com.ossanasur.cbconnect.utils.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpertiseMaterielleServiceImpl {

    private final ExpertiseMaterielleRepository repo;
    private final VictimeRepository victimeRepo;
    private final SinistreRepository sinistreRepo;
    private final ExpertRepository expertRepo;

    // ── Créer ─────────────────────────────────────────────────────
    @Transactional
    public DataResponse<ExpertiseMaterielleResponse> create(
            ExpertiseMaterielleRequest r, String loginAuteur) {

        var victime = victimeRepo.findActiveByTrackingId(r.victimeTrackingId())
                .orElseThrow(() -> new RessourceNotFoundException("Victime introuvable"));
        var sinistre = sinistreRepo.findActiveByTrackingId(r.sinistreTrackingId())
                .orElseThrow(() -> new RessourceNotFoundException("Sinistre introuvable"));

        ExpertiseMaterielle e = ExpertiseMaterielle.builder()
                .expertiseMaTrackingId(UUID.randomUUID())
                .typeExpertise(r.typeExpertise())
                .dateDemande(r.dateDemande())
                .dateRapport(r.dateRapport())
                // Véhicule
                .marqueVehicule(r.marqueVehicule())
                .modeleVehicule(r.modeleVehicule())
                .immatriculation(r.immatriculation())
                .anneeVehicule(r.anneeVehicule())
                .natureDommages(r.natureDommages())
                .estVei(r.estVei() != null && r.estVei())
                .valeurVehiculeNeuf(nvl(r.valeurVehiculeNeuf()))
                .valeurVenal(nvl(r.valeurVenal()))
                .valeurReparable(nvl(r.valeurReparable()))
                // Montants
                .montantDevis(nvl(r.montantDevis()))
                .montantDitExpert(nvl(r.montantDitExpert()))
                .honoraires(nvl(r.honoraires()))
                .observations(r.observations())
                // GED
                .ossanGedDocumentId(r.ossanGedDocumentId())
                // Liens
                .victime(victime)
                .sinistre(sinistre)
                // Audit
                .activeData(true).deletedData(false)
                .fromTable(TypeTable.EXPERTISE_MATERIELLE)
                .createdBy(loginAuteur)
                .build();

        if (r.expertTrackingId() != null)
            expertRepo.findActiveByTrackingId(r.expertTrackingId()).ifPresent(e::setExpert);

        return DataResponse.created("Rapport matériel enregistré",
                toResponse(repo.save(e)));
    }

    // ── Lire ──────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public DataResponse<ExpertiseMaterielleResponse> getByTrackingId(UUID id) {
        return DataResponse.success(toResponse(
                repo.findActiveByTrackingId(id)
                        .orElseThrow(() -> new RessourceNotFoundException("Expertise matérielle introuvable"))));
    }

    @Transactional(readOnly = true)
    public DataResponse<List<ExpertiseMaterielleResponse>> getByVictime(UUID victimeId) {
        return DataResponse.success(
                repo.findByVictime(victimeId).stream().map(this::toResponse).toList());
    }

    @Transactional(readOnly = true)
    public DataResponse<List<ExpertiseMaterielleResponse>> getBySinistre(UUID sinistreId) {
        return DataResponse.success(
                repo.findBySinistre(sinistreId).stream().map(this::toResponse).toList());
    }

    // ── Mettre à jour ─────────────────────────────────────────────
    @Transactional
    public DataResponse<ExpertiseMaterielleResponse> update(
            UUID id, ExpertiseMaterielleRequest r, String loginAuteur) {

        ExpertiseMaterielle e = repo.findActiveByTrackingId(id)
                .orElseThrow(() -> new RessourceNotFoundException("Expertise matérielle introuvable"));

        if (r.typeExpertise() != null)
            e.setTypeExpertise(r.typeExpertise());
        if (r.dateDemande() != null)
            e.setDateDemande(r.dateDemande());
        if (r.dateRapport() != null)
            e.setDateRapport(r.dateRapport());
        if (r.marqueVehicule() != null)
            e.setMarqueVehicule(r.marqueVehicule());
        if (r.modeleVehicule() != null)
            e.setModeleVehicule(r.modeleVehicule());
        if (r.immatriculation() != null)
            e.setImmatriculation(r.immatriculation());
        if (r.anneeVehicule() != null)
            e.setAnneeVehicule(r.anneeVehicule());
        if (r.natureDommages() != null)
            e.setNatureDommages(r.natureDommages());
        if (r.estVei() != null)
            e.setEstVei(r.estVei());
        if (r.valeurVehiculeNeuf() != null)
            e.setValeurVehiculeNeuf(r.valeurVehiculeNeuf());
        if (r.valeurVenal() != null)
            e.setValeurVenal(r.valeurVenal());
        if (r.valeurReparable() != null)
            e.setValeurReparable(r.valeurReparable());
        if (r.montantDevis() != null)
            e.setMontantDevis(r.montantDevis());
        if (r.montantDitExpert() != null)
            e.setMontantDitExpert(r.montantDitExpert());
        if (r.honoraires() != null)
            e.setHonoraires(r.honoraires());
        if (r.observations() != null)
            e.setObservations(r.observations());
        if (r.ossanGedDocumentId() != null)
            e.setOssanGedDocumentId(r.ossanGedDocumentId());
        if (r.expertTrackingId() != null)
            expertRepo.findActiveByTrackingId(r.expertTrackingId()).ifPresent(e::setExpert);

        e.setUpdatedBy(loginAuteur);
        return DataResponse.success("Rapport matériel mis à jour", toResponse(repo.save(e)));
    }

    // ── Mapper ────────────────────────────────────────────────────
    private ExpertiseMaterielleResponse toResponse(ExpertiseMaterielle e) {
        var v = e.getVictime();
        var s = e.getSinistre();
        var x = e.getExpert();
        return new ExpertiseMaterielleResponse(
                e.getExpertiseMaTrackingId(),
                e.getTypeExpertise(),
                e.getDateDemande(),
                e.getDateRapport(),
                v != null ? v.getVictimeTrackingId() : null,
                v != null ? v.getPrenoms() + " " + v.getNom() : null,
                s != null ? s.getSinistreTrackingId() : null,
                s != null ? s.getNumeroSinistreLocal() : null,
                x != null ? x.getNomComplet() : null,
                e.getMarqueVehicule(),
                e.getModeleVehicule(),
                e.getImmatriculation(),
                e.getAnneeVehicule(),
                e.getNatureDommages(),
                e.isEstVei(),
                e.getValeurVehiculeNeuf(),
                e.getValeurVenal(),
                e.getValeurReparable(),
                e.getMontantDevis(),
                e.getMontantDitExpert(),
                e.getHonoraires(),
                e.getObservations(),
                e.getOssanGedDocumentId(),
                e.getDateRapport() != null);
    }

    private BigDecimal nvl(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}