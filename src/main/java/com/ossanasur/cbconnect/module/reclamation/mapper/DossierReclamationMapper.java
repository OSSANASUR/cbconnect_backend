package com.ossanasur.cbconnect.module.reclamation.mapper;

import com.ossanasur.cbconnect.common.enums.StatutPiece;
import com.ossanasur.cbconnect.module.reclamation.dto.response.DossierReclamationResponse;
import com.ossanasur.cbconnect.module.reclamation.dto.response.FactureReclamationResponse;
import com.ossanasur.cbconnect.module.reclamation.entity.DossierReclamation;
import com.ossanasur.cbconnect.module.reclamation.entity.FactureReclamation;
import com.ossanasur.cbconnect.module.reclamation.entity.PieceDossierReclamation;
import com.ossanasur.cbconnect.module.reclamation.repository.FactureReclamationRepository;
import com.ossanasur.cbconnect.module.reclamation.repository.PieceDossierReclamationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DossierReclamationMapper {

    private final PieceDossierReclamationRepository pieceRepository;
    private final FactureReclamationRepository factureRepository;

    /**
     * Mapping minimal pour la liste paginée : sans la collection de factures,
     * mais avec les compteurs (pièces + factures) et les infos agrégées.
     */
    public DossierReclamationResponse toListResponse(DossierReclamation d) {
        if (d == null) return null;
        var counts = computePieceCounts(d);
        long nbFactures = factureRepository.findByDossier(d.getDossierTrackingId()).size();
        return build(d, counts, (int) nbFactures, null);
    }

    /**
     * Mapping complet pour la vue détail : inclut la liste des factures.
     */
    public DossierReclamationResponse toResponse(DossierReclamation d) {
        if (d == null) return null;
        var counts = computePieceCounts(d);
        List<FactureReclamationResponse> factures = factureRepository.findByDossier(d.getDossierTrackingId())
                .stream().map(this::toFactureResponse).collect(Collectors.toList());
        return build(d, counts, factures.size(), factures);
    }

    public FactureReclamationResponse toFactureResponse(FactureReclamation f) {
        if (f == null) return null;
        return new FactureReclamationResponse(
                f.getFactureTrackingId(), f.getNumeroFactureOriginal(), f.getTypeDepense(),
                f.getNomPrestataire(), f.getDateFacture(), f.getMontantReclame(), f.getMontantRetenu(),
                f.getStatutTraitement(), f.getMotifRejet(), f.isLienAvecAccidentVerifie(),
                f.getDateTraitement(), f.getOssanGedDocumentId()
        );
    }

    // ── interne ────────────────────────────────────────────────────
    private DossierReclamationResponse build(
            DossierReclamation d, PieceCounts c, int nbFactures, List<FactureReclamationResponse> factures) {
        return new DossierReclamationResponse(
                d.getDossierTrackingId(), d.getNumeroDossier(), d.getDateOuverture(), d.getDateCloture(),
                d.getStatut(), d.getMontantTotalReclame(), d.getMontantTotalRetenu(), d.getNotesRedacteur(),
                // sinistre
                d.getSinistre() != null ? d.getSinistre().getSinistreTrackingId() : null,
                d.getSinistre() != null ? d.getSinistre().getNumeroSinistreLocal() : null,
                d.getSinistre() != null ? d.getSinistre().getTypeDommage() : null,
                d.getSinistre() != null ? d.getSinistre().getDateAccident() : null,
                // victime
                d.getVictime() != null ? d.getVictime().getVictimeTrackingId() : null,
                d.getVictime() != null ? (d.getVictime().getNom() + " " + d.getVictime().getPrenoms()) : null,
                d.getVictime() != null ? d.getVictime().getTypeVictime() : null,
                d.getVictime() != null ? d.getVictime().getDateNaissance() : null,
                // redacteur
                d.getRedacteur() != null
                        ? (d.getRedacteur().getNom() + " " + d.getRedacteur().getPrenoms())
                        : null,
                // pieces
                c.requises, c.recues, c.attendues, c.rejetees, c.estMur,
                // factures counts
                nbFactures, (int) c.recues.longValue(),
                factures
        );
    }

    private PieceCounts computePieceCounts(DossierReclamation d) {
        List<PieceDossierReclamation> pieces = pieceRepository.findByDossier(d.getHistoriqueId());
        long requises = pieces.stream().filter(p -> p.getTypePiece() != null && p.getTypePiece().isObligatoire()).count();
        long recues = pieces.stream().filter(p -> p.getStatut() == StatutPiece.RECUE).count();
        long attendues = pieces.stream().filter(p -> p.getStatut() == StatutPiece.ATTENDUE).count();
        long rejetees = pieces.stream().filter(p -> p.getStatut() == StatutPiece.REJETEE).count();
        long recuesObligatoires = pieces.stream()
                .filter(p -> p.getTypePiece() != null && p.getTypePiece().isObligatoire())
                .filter(p -> p.getStatut() == StatutPiece.RECUE)
                .count();
        boolean estMur = requises > 0 && recuesObligatoires == requises;
        return new PieceCounts(requises, recues, attendues, rejetees, estMur);
    }

    private record PieceCounts(Long requises, Long recues, Long attendues, Long rejetees, Boolean estMur) {}
}
