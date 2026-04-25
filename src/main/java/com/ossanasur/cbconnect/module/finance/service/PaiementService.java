package com.ossanasur.cbconnect.module.finance.service;

import com.ossanasur.cbconnect.common.enums.StatutPaiement;
import com.ossanasur.cbconnect.module.finance.dto.request.AnnulerPaiementRequest;
import com.ossanasur.cbconnect.module.finance.dto.request.PaiementCreateRequest;
import com.ossanasur.cbconnect.module.finance.dto.request.ReglementComptableRequest;
import com.ossanasur.cbconnect.module.finance.dto.response.PaiementDetailResponse;
import com.ossanasur.cbconnect.module.finance.dto.response.PaiementResponse;
import com.ossanasur.cbconnect.utils.DataResponse;
import com.ossanasur.cbconnect.utils.PaginatedResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PaiementService {

        DataResponse<PaiementDetailResponse> creer(PaiementCreateRequest request, String loginAuteur);

        DataResponse<PaiementDetailResponse> validerTechnique(UUID paiementTrackingId, String loginAuteur);

        DataResponse<PaiementDetailResponse> saisirReglementComptable(UUID paiementTrackingId,
                        ReglementComptableRequest request, String loginAuteur);

        DataResponse<PaiementDetailResponse> validerComptable(UUID paiementTrackingId, String loginAuteur);

        DataResponse<PaiementDetailResponse> annuler(UUID paiementTrackingId,
                        AnnulerPaiementRequest request, String loginAuteur);

        DataResponse<PaiementDetailResponse> lierEncaissement(UUID paiementTrackingId,
                        UUID encaissementTrackingId, String loginAuteur);

        DataResponse<PaiementDetailResponse> delierEncaissement(UUID paiementTrackingId,
                        UUID encaissementTrackingId, String loginAuteur);

        DataResponse<PaiementDetailResponse> getByTrackingId(UUID paiementTrackingId);

        DataResponse<List<PaiementResponse>> getBySinistre(UUID sinistreTrackingId);

        PaginatedResponse<PaiementResponse> rechercher(StatutPaiement statut, LocalDate dateDebut,
                        LocalDate dateFin, UUID sinistreTrackingId, int page, int size);
}