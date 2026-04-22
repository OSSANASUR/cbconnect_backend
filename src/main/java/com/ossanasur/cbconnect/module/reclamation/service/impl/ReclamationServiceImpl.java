package com.ossanasur.cbconnect.module.reclamation.service.impl;

import com.ossanasur.cbconnect.common.enums.*;
import com.ossanasur.cbconnect.exception.BadRequestException;
import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.module.auth.repository.UtilisateurRepository;
import com.ossanasur.cbconnect.module.ged.dto.request.UploadDocumentRequest;
import com.ossanasur.cbconnect.module.ged.dto.response.DocumentGedResponse;
import com.ossanasur.cbconnect.module.ged.service.OssanGedClientService;
import com.ossanasur.cbconnect.module.reclamation.dto.request.*;
import com.ossanasur.cbconnect.module.reclamation.dto.response.*;
import com.ossanasur.cbconnect.module.reclamation.entity.*;
import com.ossanasur.cbconnect.module.reclamation.mapper.DossierReclamationMapper;
import com.ossanasur.cbconnect.module.reclamation.repository.*;
import com.ossanasur.cbconnect.module.reclamation.service.PiecesAdministrativesService;
import com.ossanasur.cbconnect.module.reclamation.service.ReclamationService;
import com.ossanasur.cbconnect.module.sinistre.repository.*;
import com.ossanasur.cbconnect.utils.DataResponse;
import com.ossanasur.cbconnect.utils.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReclamationServiceImpl implements ReclamationService {

    private static final long MAX_FILE_SIZE_BYTES = 20L * 1024 * 1024;

    private final DossierReclamationRepository dossierRepository;
    private final FactureReclamationRepository factureRepository;
    private final SinistreRepository sinistreRepository;
    private final VictimeRepository victimeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final DossierReclamationMapper mapper;
    private final PiecesAdministrativesService piecesService;
    private final OssanGedClientService gedService;

    // ── DOSSIERS ────────────────────────────────────────────────────

    @Override
    @Transactional
    public DataResponse<DossierReclamationResponse> ouvrirDossier(DossierReclamationRequest r, String loginAuteur) {
        var sinistre = sinistreRepository.findActiveByTrackingId(r.sinistreTrackingId())
                .orElseThrow(() -> new RessourceNotFoundException("Sinistre introuvable"));
        var victime = victimeRepository.findActiveByTrackingId(r.victimeTrackingId())
                .orElseThrow(() -> new RessourceNotFoundException("Victime introuvable"));
        long count = dossierRepository.count() + 1;
        String numero = "REC-" + LocalDate.now().getYear() + "-" + String.format("%05d", count);
        DossierReclamation d = DossierReclamation.builder()
                .dossierTrackingId(UUID.randomUUID()).numeroDossier(numero)
                .dateOuverture(LocalDate.now()).statut(StatutDossierReclamation.OUVERT)
                .montantTotalReclame(BigDecimal.ZERO).montantTotalRetenu(BigDecimal.ZERO)
                .notesRedacteur(r.notesRedacteur())
                .sinistre(sinistre).victime(victime)
                .createdBy(loginAuteur).activeData(true).deletedData(false)
                .fromTable(TypeTable.DOSSIER_RECLAMATION).build();
        if (r.redacteurTrackingId() != null) {
            utilisateurRepository
                    .findByUtilisateurTrackingIdAndActiveDataTrueAndDeletedDataFalse(r.redacteurTrackingId())
                    .ifPresent(d::setRedacteur);
        }
        DossierReclamation saved = dossierRepository.save(d);

        // Initialiser automatiquement les pièces ATTENDUE selon le type de dommage du sinistre
        try {
            piecesService.initialiserPiecesDossier(saved.getDossierTrackingId(), loginAuteur);
        } catch (Exception e) {
            log.warn("Initialisation des pièces du dossier {} échouée : {}", numero, e.getMessage());
        }

        return DataResponse.created("Dossier reclamation ouvert : " + numero, mapper.toResponse(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<DossierReclamationResponse> getDossier(UUID id) {
        return DataResponse.success(mapper.toResponse(dossierRepository.findActiveByTrackingId(id)
                .orElseThrow(() -> new RessourceNotFoundException("Dossier introuvable"))));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<DossierReclamationResponse>> getDossiersByVictime(UUID victimeId) {
        return DataResponse.success(dossierRepository.findByVictime(victimeId).stream()
                .map(mapper::toListResponse).collect(Collectors.toList()));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<DossierReclamationResponse> listerDossiers(
            StatutDossierReclamation statut, String search, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        Page<DossierReclamation> p = dossierRepository.search(statut, search, pageable);
        Page<DossierReclamationResponse> mapped = p.map(mapper::toListResponse);
        return PaginatedResponse.fromPage(mapped, "Liste des dossiers de réclamation");
    }

    @Override
    @Transactional
    public DataResponse<Void> clotureDossier(UUID id, String loginAuteur) {
        var dossier = dossierRepository.findActiveByTrackingId(id)
                .orElseThrow(() -> new RessourceNotFoundException("Dossier introuvable"));
        dossier.setStatut(StatutDossierReclamation.CLOTURE);
        dossier.setDateCloture(LocalDate.now());
        dossier.setUpdatedBy(loginAuteur);
        dossierRepository.save(dossier);
        return DataResponse.success("Dossier cloture", null);
    }

    // ── FACTURES ────────────────────────────────────────────────────

    @Override
    @Transactional
    public DataResponse<FactureReclamationResponse> ajouterFacture(FactureReclamationRequest r, String loginAuteur) {
        if (r.dossierTrackingId() == null) {
            throw new BadRequestException("dossierTrackingId requis");
        }
        return ajouterFactureDansDossier(r.dossierTrackingId(), r, loginAuteur);
    }

    @Override
    @Transactional
    public DataResponse<FactureReclamationResponse> ajouterFactureDansDossier(
            UUID dossierId, FactureReclamationRequest r, String loginAuteur) {
        var dossier = dossierRepository.findActiveByTrackingId(dossierId)
                .orElseThrow(() -> new RessourceNotFoundException("Dossier introuvable"));
        FactureReclamation f = FactureReclamation.builder()
                .factureTrackingId(UUID.randomUUID()).numeroFactureOriginal(r.numeroFactureOriginal())
                .typeDepense(r.typeDepense()).nomPrestataire(r.nomPrestataire())
                .dateFacture(r.dateFacture()).montantReclame(r.montantReclame())
                .statutTraitement(StatutTraitementFacture.EN_ATTENTE).dossierReclamation(dossier)
                .lienAvecAccidentVerifie(Boolean.TRUE.equals(r.lienAvecAccidentVerifie()))
                .createdBy(loginAuteur).activeData(true).deletedData(false)
                .fromTable(TypeTable.FACTURE_RECLAMATION).build();

        // Pré-validation à la création : si le rédacteur a déjà tranché sur le montant,
        // on applique directement le statut final (VALIDE / VALIDE_PARTIELLEMENT / REJETE).
        if (r.montantRetenu() != null) {
            BigDecimal retenu = r.montantRetenu();
            if (retenu.signum() < 0 || retenu.compareTo(r.montantReclame()) > 0) {
                throw new BadRequestException(
                        "Le montant retenu doit être compris entre 0 et le montant réclamé");
            }
            if (retenu.signum() == 0) {
                if (r.motifRejet() == null || r.motifRejet().isBlank()) {
                    throw new BadRequestException("Motif requis pour un rejet");
                }
                f.setStatutTraitement(StatutTraitementFacture.REJETE);
                f.setMotifRejet(r.motifRejet().trim());
            } else if (retenu.compareTo(r.montantReclame()) < 0) {
                if (r.motifRejet() == null || r.motifRejet().isBlank()) {
                    throw new BadRequestException("Motif requis pour une validation partielle");
                }
                f.setStatutTraitement(StatutTraitementFacture.VALIDE_PARTIELLEMENT);
                f.setMotifRejet(r.motifRejet().trim());
            } else {
                f.setStatutTraitement(StatutTraitementFacture.VALIDE);
            }
            f.setMontantRetenu(retenu);
            f.setDateTraitement(LocalDate.now());
        }

        FactureReclamation saved = factureRepository.save(f);

        // Mise à jour des totaux du dossier
        dossier.setMontantTotalReclame(dossier.getMontantTotalReclame().add(r.montantReclame()));
        if (saved.getMontantRetenu() != null) {
            dossier.setMontantTotalRetenu(dossier.getMontantTotalRetenu().add(saved.getMontantRetenu()));
        }
        dossier.setUpdatedBy(loginAuteur);
        dossierRepository.save(dossier);

        return DataResponse.created("Facture ajoutée", mapper.toFactureResponse(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<FactureReclamationResponse>> getFacturesByDossier(UUID dossierId) {
        return DataResponse.success(factureRepository.findByDossier(dossierId).stream()
                .map(mapper::toFactureResponse).collect(Collectors.toList()));
    }

    @Override
    @Transactional
    public DataResponse<FactureReclamationResponse> updateFacture(
            UUID id, UpdateFactureRequest req, String loginAuteur) {
        FactureReclamation f = factureRepository.findActiveByTrackingId(id)
                .orElseThrow(() -> new RessourceNotFoundException("Facture introuvable"));

        BigDecimal ancienRetenu = f.getMontantRetenu() != null ? f.getMontantRetenu() : BigDecimal.ZERO;
        BigDecimal nouveauRetenu;
        StatutTraitementFacture nouveauStatut = req.statutTraitement();

        switch (nouveauStatut) {
            case VALIDE -> {
                nouveauRetenu = f.getMontantReclame();
                f.setMotifRejet(null);
            }
            case VALIDE_PARTIELLEMENT -> {
                if (req.montantRetenu() == null
                        || req.montantRetenu().signum() <= 0
                        || req.montantRetenu().compareTo(f.getMontantReclame()) >= 0) {
                    throw new BadRequestException(
                            "Le montant retenu doit être compris entre 1 et le montant réclamé exclu");
                }
                if (req.motifRejet() == null || req.motifRejet().isBlank()) {
                    throw new BadRequestException("Le motif est requis pour une validation partielle");
                }
                nouveauRetenu = req.montantRetenu();
                f.setMotifRejet(req.motifRejet().trim());
            }
            case REJETE -> {
                if (req.motifRejet() == null || req.motifRejet().isBlank()) {
                    throw new BadRequestException("Le motif est requis pour un rejet");
                }
                nouveauRetenu = BigDecimal.ZERO;
                f.setMotifRejet(req.motifRejet().trim());
            }
            case EN_ATTENTE -> {
                nouveauRetenu = BigDecimal.ZERO;
                f.setMotifRejet(null);
            }
            default -> throw new BadRequestException("Statut non géré : " + nouveauStatut);
        }

        f.setMontantRetenu(nouveauStatut == StatutTraitementFacture.EN_ATTENTE ? null : nouveauRetenu);
        f.setStatutTraitement(nouveauStatut);
        f.setLienAvecAccidentVerifie(nouveauStatut != StatutTraitementFacture.EN_ATTENTE);
        f.setDateTraitement(nouveauStatut == StatutTraitementFacture.EN_ATTENTE ? null : LocalDate.now());
        f.setUpdatedBy(loginAuteur);
        factureRepository.save(f);

        // Fix : délta sur le total retenu du dossier (évite le double-count sur update)
        BigDecimal delta = nouveauRetenu.subtract(ancienRetenu);
        var dossier = f.getDossierReclamation();
        dossier.setMontantTotalRetenu(dossier.getMontantTotalRetenu().add(delta));
        dossier.setUpdatedBy(loginAuteur);
        dossierRepository.save(dossier);

        return DataResponse.success("Facture mise à jour", mapper.toFactureResponse(f));
    }

    @Override
    @Transactional
    public DataResponse<Void> supprimerFacture(UUID id, String loginAuteur) {
        FactureReclamation f = factureRepository.findActiveByTrackingId(id)
                .orElseThrow(() -> new RessourceNotFoundException("Facture introuvable"));
        if (f.getStatutTraitement() != StatutTraitementFacture.EN_ATTENTE) {
            throw new BadRequestException(
                    "Seules les factures en attente peuvent être supprimées");
        }
        // Déduire le montant réclamé du total dossier
        var dossier = f.getDossierReclamation();
        dossier.setMontantTotalReclame(dossier.getMontantTotalReclame().subtract(f.getMontantReclame()));
        dossier.setUpdatedBy(loginAuteur);
        dossierRepository.save(dossier);

        f.setActiveData(false);
        f.setDeletedData(true);
        f.setUpdatedBy(loginAuteur);
        factureRepository.save(f);
        return DataResponse.success("Facture supprimée", null);
    }

    @Override
    @Transactional
    public DataResponse<FactureReclamationResponse> attacherDocumentFacture(
            UUID factureId, MultipartFile file, String titre, String typeDocument, String loginAuteur) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Le fichier est requis");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException("Le fichier dépasse la taille maximale autorisée (20 Mo)");
        }
        FactureReclamation f = factureRepository.findActiveByTrackingId(factureId)
                .orElseThrow(() -> new RessourceNotFoundException("Facture introuvable"));

        TypeDocumentOssanGed typeGed;
        try {
            typeGed = (typeDocument == null || typeDocument.isBlank())
                    ? TypeDocumentOssanGed.FACTURE_RECLAMATION
                    : TypeDocumentOssanGed.valueOf(typeDocument.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            typeGed = TypeDocumentOssanGed.FACTURE_RECLAMATION;
        }

        String titreFinal = (titre != null && !titre.isBlank())
                ? titre
                : ("Facture " + f.getNomPrestataire()
                        + (f.getNumeroFactureOriginal() != null ? " — " + f.getNumeroFactureOriginal() : ""));

        var dossier = f.getDossierReclamation();
        UUID sinistreId = dossier.getSinistre() != null ? dossier.getSinistre().getSinistreTrackingId() : null;
        UUID victimeId = dossier.getVictime() != null ? dossier.getVictime().getVictimeTrackingId() : null;

        UploadDocumentRequest req = new UploadDocumentRequest(
                titreFinal, typeGed, f.getDateFacture(),
                sinistreId, victimeId, dossier.getDossierTrackingId(), null, null);

        DataResponse<DocumentGedResponse> res = gedService.uploadDocument(file, req, loginAuteur);
        if (res == null || res.getData() == null || res.getData().ossanGedDocumentId() == null) {
            throw new BadRequestException("Upload GED échoué — réessayez ou contactez un administrateur");
        }
        f.setOssanGedDocumentId(res.getData().ossanGedDocumentId());
        f.setUpdatedBy(loginAuteur);
        factureRepository.save(f);

        return DataResponse.created("Facture archivée dans la GED", mapper.toFactureResponse(f));
    }
}
