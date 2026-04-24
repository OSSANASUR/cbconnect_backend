package com.ossanasur.cbconnect.module.finance.service;

import com.ossanasur.cbconnect.common.enums.StatutPaiement;
import com.ossanasur.cbconnect.module.finance.dto.request.AnnulerPaiementRequest;
import com.ossanasur.cbconnect.module.finance.dto.request.MarquerPayeRequest;
import com.ossanasur.cbconnect.module.finance.dto.request.PaiementCreateRequest;
import com.ossanasur.cbconnect.module.finance.dto.response.PaiementDetailResponse;
import com.ossanasur.cbconnect.module.finance.dto.response.PaiementResponse;
import com.ossanasur.cbconnect.utils.DataResponse;
import com.ossanasur.cbconnect.utils.PaginatedResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Règlements sinistres — phase technique (bénéficiaire, montant, chèque) et
 * transitions d'état vers la phase comptable (paiement effectif, annulation,
 * rapprochement encaissement).
 *
 * <p>
 * <strong>Immutabilité :</strong> un règlement ne se modifie pas. Pour corriger
 * une erreur, on annule le paiement existant et on en crée un nouveau.
 */
public interface PaiementService {

    /**
     * Crée un paiement (statut EMIS) et génère automatiquement l'écriture
     * comptable associée (BROUILLON, type PAIEMENT_VICTIME) dans la même
     * transaction.
     */
    DataResponse<PaiementDetailResponse> creer(PaiementCreateRequest request, String loginAuteur);

    /**
     * Annule un paiement. Si l'écriture comptable était VALIDEE, génère une
     * CONTRA_ECRITURE ; sinon passe simplement l'écriture BROUILLON en ANNULEE.
     */
    DataResponse<PaiementDetailResponse> annuler(UUID paiementTrackingId, AnnulerPaiementRequest request,
            String loginAuteur);

    /**
     * Transition EMIS → PAYE : exige que l'écriture soit déjà VALIDEE.
     * Réservé au rôle COMPTABLE.
     */
    DataResponse<PaiementDetailResponse> marquerPaye(UUID paiementTrackingId, MarquerPayeRequest request,
            String loginAuteur);

    /** Rattache un encaissement à un paiement déjà PAYE. */
    DataResponse<PaiementDetailResponse> lierEncaissement(UUID paiementTrackingId, UUID encaissementTrackingId,
            String loginAuteur);

    /** Détache un encaissement précédemment rattaché. */
    DataResponse<PaiementDetailResponse> delierEncaissement(UUID paiementTrackingId, UUID encaissementTrackingId,
            String loginAuteur);

    /** Détail complet d'un paiement (bénéficiaire, écriture, encaissements, audit). */
    DataResponse<PaiementDetailResponse> getByTrackingId(UUID paiementTrackingId);

    /** Tous les paiements actifs d'un sinistre, tri date d'émission DESC. */
    DataResponse<List<PaiementResponse>> getBySinistre(UUID sinistreTrackingId);

    /** Recherche paginée avec filtres optionnels. */
    PaginatedResponse<PaiementResponse> rechercher(StatutPaiement statut, LocalDate dateDebut, LocalDate dateFin,
            UUID sinistreTrackingId, int page, int size);
}
