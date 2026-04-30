package com.ossanasur.cbconnect.module.reclamation.repository;

import com.ossanasur.cbconnect.common.enums.TypeDocumentOssanGed;
import com.ossanasur.cbconnect.module.reclamation.entity.PieceDossierReclamation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PieceDossierReclamationRepository extends JpaRepository<PieceDossierReclamation, Integer> {

    /**
     * Toutes les pièces d'un dossier, triées par type (commun en premier) puis
     * ordre
     */
    @Query("SELECT p FROM PieceDossierReclamation p " +
            "JOIN FETCH p.typePiece t " +
            "WHERE p.dossierReclamation.historiqueId = :dossierId " +
            "AND p.deletedData = false " +
            "ORDER BY CASE WHEN t.typeDommage IS NULL THEN 0 ELSE 1 END, t.ordre")
    List<PieceDossierReclamation> findByDossier(@Param("dossierId") int dossierId);

    /**
     * Vérifie si toutes les pièces obligatoires d'un dossier sont RECUES → dossier
     * mûr
     */
    @Query("SELECT COUNT(p) = 0 FROM PieceDossierReclamation p " +
            "JOIN p.typePiece t " +
            "WHERE p.dossierReclamation.historiqueId = :dossierId " +
            "AND p.deletedData = false " +
            "AND t.obligatoire = true " +
            "AND p.statut <> com.ossanasur.cbconnect.common.enums.StatutPiece.RECUE")
    boolean isDossierMur(@Param("dossierId") int dossierId);

    /**
     * Première pièce ATTENDUE d'un dossier dont le type est configuré pour
     * l'auto-association avec un type de document GED donné.
     * Utilisé pour l'auto-association à l'upload.
     */
    @Query("SELECT p FROM PieceDossierReclamation p " +
            "JOIN p.typePiece t " +
            "WHERE p.dossierReclamation.historiqueId = :dossierId " +
            "AND p.deletedData = false " +
            "AND p.statut = com.ossanasur.cbconnect.common.enums.StatutPiece.ATTENDUE " +
            "AND t.typeDocumentGed = :typeDocGed " +
            "ORDER BY t.ordre")
    List<PieceDossierReclamation> findAttendusByDossierAndTypeGed(
            @Param("dossierId") int dossierId,
            @Param("typeDocGed") TypeDocumentOssanGed typeDocGed);

    Optional<PieceDossierReclamation> findByTrackingId(UUID trackingId);

    boolean existsByDossierReclamation_HistoriqueIdAndTypePiece_HistoriqueId(
            int dossierReclamationId, int typePieceId);
}