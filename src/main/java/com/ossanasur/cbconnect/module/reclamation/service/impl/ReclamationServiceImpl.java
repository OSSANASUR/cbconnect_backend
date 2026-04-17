package com.ossanasur.cbconnect.module.reclamation.service.impl;
import com.ossanasur.cbconnect.common.enums.*;
import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.module.auth.repository.UtilisateurRepository;
import com.ossanasur.cbconnect.module.reclamation.dto.request.*;
import com.ossanasur.cbconnect.module.reclamation.dto.response.*;
import com.ossanasur.cbconnect.module.reclamation.entity.*;
import com.ossanasur.cbconnect.module.reclamation.mapper.DossierReclamationMapper;
import com.ossanasur.cbconnect.module.reclamation.repository.*;
import com.ossanasur.cbconnect.module.reclamation.service.ReclamationService;
import com.ossanasur.cbconnect.module.sinistre.repository.*;
import com.ossanasur.cbconnect.utils.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@Service @RequiredArgsConstructor
public class ReclamationServiceImpl implements ReclamationService {
    private final DossierReclamationRepository dossierRepository;
    private final FactureReclamationRepository factureRepository;
    private final SinistreRepository sinistreRepository;
    private final VictimeRepository victimeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final DossierReclamationMapper mapper;

    @Override @Transactional
    public DataResponse<DossierReclamationResponse> ouvrirDossier(DossierReclamationRequest r, String loginAuteur) {
        var sinistre = sinistreRepository.findActiveByTrackingId(r.sinistreTrackingId())
            .orElseThrow(() -> new RessourceNotFoundException("Sinistre introuvable"));
        var victime = victimeRepository.findActiveByTrackingId(r.victimeTrackingId())
            .orElseThrow(() -> new RessourceNotFoundException("Victime introuvable"));
        // Generation numero dossier : REC-AAAA-NNNNN
        long count = dossierRepository.count() + 1;
        String numero = "REC-" + LocalDate.now().getYear() + "-" + String.format("%05d", count);
        DossierReclamation d = DossierReclamation.builder()
            .dossierTrackingId(UUID.randomUUID()).numeroDossier(numero)
            .dateOuverture(LocalDate.now()).statut(StatutDossierReclamation.OUVERT)
            .montantTotalReclame(BigDecimal.ZERO).montantTotalRetenu(BigDecimal.ZERO)
            .sinistre(sinistre).victime(victime)
            .createdBy(loginAuteur).activeData(true).deletedData(false)
            .fromTable(TypeTable.DOSSIER_RECLAMATION).build();
        if(r.redacteurTrackingId()!=null)
            utilisateurRepository.findByUtilisateurTrackingIdAndActiveDataTrueAndDeletedDataFalse(r.redacteurTrackingId()).ifPresent(d::setRedacteur);
        return DataResponse.created("Dossier reclamation ouvert : " + numero, mapper.toResponse(dossierRepository.save(d)));
    }

    @Override @Transactional(readOnly=true)
    public DataResponse<DossierReclamationResponse> getDossier(UUID id) {
        return DataResponse.success(mapper.toResponse(dossierRepository.findActiveByTrackingId(id)
            .orElseThrow(() -> new RessourceNotFoundException("Dossier introuvable"))));
    }

    @Override @Transactional(readOnly=true)
    public DataResponse<List<DossierReclamationResponse>> getDossiersByVictime(UUID victimeId) {
        return DataResponse.success(dossierRepository.findByVictime(victimeId).stream().map(mapper::toResponse).collect(Collectors.toList()));
    }

    @Override @Transactional
    public DataResponse<FactureReclamationResponse> ajouterFacture(FactureReclamationRequest r, String loginAuteur) {
        var dossier = dossierRepository.findActiveByTrackingId(r.dossierTrackingId())
            .orElseThrow(() -> new RessourceNotFoundException("Dossier introuvable"));
        FactureReclamation f = FactureReclamation.builder()
            .factureTrackingId(UUID.randomUUID()).numeroFactureOriginal(r.numeroFactureOriginal())
            .typeDepense(r.typeDepense()).nomPrestataire(r.nomPrestataire())
            .dateFacture(r.dateFacture()).montantReclame(r.montantReclame())
            .statutTraitement(StatutTraitementFacture.EN_ATTENTE).dossierReclamation(dossier)
            .createdBy(loginAuteur).activeData(true).deletedData(false)
            .fromTable(TypeTable.FACTURE_RECLAMATION).build();
        FactureReclamation saved = factureRepository.save(f);
        // Mettre a jour total reclame du dossier
        dossier.setMontantTotalReclame(dossier.getMontantTotalReclame().add(r.montantReclame()));
        dossier.setUpdatedBy(loginAuteur); dossierRepository.save(dossier);
        return DataResponse.created("Facture ajoutee", mapper.toFactureResponse(saved));
    }

    @Override @Transactional
    public DataResponse<FactureReclamationResponse> validerFacture(UUID id, BigDecimal montantRetenu, String loginAuteur) {
        FactureReclamation f = factureRepository.findActiveByTrackingId(id)
            .orElseThrow(() -> new RessourceNotFoundException("Facture introuvable"));
        f.setMontantRetenu(montantRetenu);
        f.setStatutTraitement(montantRetenu.compareTo(f.getMontantReclame())<0 ? StatutTraitementFacture.VALIDE_PARTIELLEMENT : StatutTraitementFacture.VALIDE);
        f.setLienAvecAccidentVerifie(true); f.setDateTraitement(LocalDate.now()); f.setUpdatedBy(loginAuteur);
        factureRepository.save(f);
        // Mettre a jour total retenu du dossier
        var dossier = f.getDossierReclamation();
        BigDecimal ancienRetenu = f.getMontantRetenu() != null ? f.getMontantRetenu() : BigDecimal.ZERO;
        dossier.setMontantTotalRetenu(dossier.getMontantTotalRetenu().add(montantRetenu));
        dossier.setUpdatedBy(loginAuteur); dossierRepository.save(dossier);
        return DataResponse.success("Facture validee", mapper.toFactureResponse(f));
    }

    @Override @Transactional
    public DataResponse<FactureReclamationResponse> rejeterFacture(UUID id, String motif, String loginAuteur) {
        FactureReclamation f = factureRepository.findActiveByTrackingId(id)
            .orElseThrow(() -> new RessourceNotFoundException("Facture introuvable"));
        f.setStatutTraitement(StatutTraitementFacture.REJETE); f.setMotifRejet(motif);
        f.setDateTraitement(LocalDate.now()); f.setUpdatedBy(loginAuteur);
        return DataResponse.success("Facture rejetee", mapper.toFactureResponse(factureRepository.save(f)));
    }

    @Override @Transactional(readOnly=true)
    public DataResponse<List<FactureReclamationResponse>> getFacturesByDossier(UUID dossierId) {
        return DataResponse.success(factureRepository.findByDossier(dossierId).stream().map(mapper::toFactureResponse).collect(Collectors.toList()));
    }

    @Override @Transactional
    public DataResponse<Void> clotureDossier(UUID id, String loginAuteur) {
        var dossier = dossierRepository.findActiveByTrackingId(id)
            .orElseThrow(() -> new RessourceNotFoundException("Dossier introuvable"));
        dossier.setStatut(StatutDossierReclamation.CLOTURE); dossier.setDateCloture(LocalDate.now()); dossier.setUpdatedBy(loginAuteur);
        dossierRepository.save(dossier);
        return DataResponse.success("Dossier cloture", null);
    }
}
