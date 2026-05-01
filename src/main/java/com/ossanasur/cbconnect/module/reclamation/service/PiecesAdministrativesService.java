package com.ossanasur.cbconnect.module.reclamation.service;

import com.ossanasur.cbconnect.common.enums.TypeDocumentOssanGed;
import com.ossanasur.cbconnect.module.reclamation.dto.request.AssocierDocumentRequest;

import java.time.LocalDate;
import com.ossanasur.cbconnect.module.reclamation.dto.request.TypePieceRequest;
import com.ossanasur.cbconnect.module.reclamation.dto.response.MaturiteDossierResponse;
import com.ossanasur.cbconnect.module.reclamation.dto.response.PieceDossierResponse;
import com.ossanasur.cbconnect.module.reclamation.dto.response.TypePieceResponse;
import com.ossanasur.cbconnect.utils.DataResponse;

import java.util.List;
import java.util.UUID;

public interface PiecesAdministrativesService {

    // ── Paramétrage (admin) ───────────────────────────────────────
    DataResponse<List<TypePieceResponse>> listerTypesPieces();

    DataResponse<TypePieceResponse> creerTypePiece(TypePieceRequest req, String loginAuteur);

    DataResponse<TypePieceResponse> modifierTypePiece(UUID trackingId, TypePieceRequest req, String loginAuteur);

    DataResponse<Void> toggleActif(UUID trackingId, String loginAuteur);

    // ── Gestion par dossier ───────────────────────────────────────

    /**
     * Initialise les pièces ATTENDUE d'un dossier à l'ouverture.
     * Appelé automatiquement par ReclamationService.ouvrirDossier().
     * Crée une entrée PieceDossierReclamation ATTENDUE pour chaque TypePiece
     * applicable au TypeDommage du sinistre.
     */
    void initialiserPiecesDossier(UUID dossierTrackingId, String loginAuteur);

    /** Liste toutes les pièces d'un dossier avec leur statut */
    DataResponse<MaturiteDossierResponse> getMaturiteDossier(UUID dossierTrackingId);

    /**
     * Associe un document GED à une pièce → statut passe à RECUE.
     * Si la pièce était REJETEE, elle repasse à RECUE avec le nouveau doc.
     */
    DataResponse<PieceDossierResponse> associerDocument(
            UUID pieceDossierTrackingId,
            AssocierDocumentRequest req,
            String loginAuteur);

    /** Rejette un document associé → statut passe à REJETEE */
    DataResponse<PieceDossierResponse> rejeterDocument(
            UUID pieceDossierTrackingId,
            String notes,
            String loginAuteur);

    /** Désassocie le doc GED → statut repasse à ATTENDUE */
    DataResponse<PieceDossierResponse> retirerDocument(
            UUID pieceDossierTrackingId,
            String loginAuteur);

    /**
     * Auto-association déclenchée après un upload GED.
     * Si le dossier possède une pièce ATTENDUE dont typeDocumentGed correspond
     * au type du document uploadé, elle est automatiquement marquée RECUE.
     * Idempotent et silencieux (ne lève pas d'exception).
     */
    void autoAssocierParTypeDocument(
            UUID dossierTrackingId,
            TypeDocumentOssanGed typeDocumentGed,
            UUID ossanGedDocumentTrackingId,
            String loginAuteur);

    /**
     * Auto-création d'une FactureReclamation après upload GED d'un document de type facture.
     * Applicable pour : FACTURE_MED, FACTURE_RECLAMATION, FRAIS_FUNERAIRES, FACTURE_ATTESTATION.
     * La facture est créée avec montantReclame=0 et nomPrestataire=titre (à compléter via UI).
     * Silencieux — ne lève pas d'exception.
     */
    void autoCreerFactureDepuisGed(
            UUID dossierTrackingId,
            TypeDocumentOssanGed typeDocumentGed,
            String titre,
            LocalDate dateDocument,
            Integer ossanGedDocumentId,
            String loginAuteur);
}