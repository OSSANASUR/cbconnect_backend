package com.ossanasur.cbconnect.module.reclamation.service;

import com.ossanasur.cbconnect.module.reclamation.dto.request.AssocierDocumentRequest;
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
}