package com.ossanasur.cbconnect.module.finance.service;

import com.ossanasur.cbconnect.module.finance.dto.request.AnnulerPrefinancementRequest;
import com.ossanasur.cbconnect.module.finance.dto.request.PrefinancementCreateRequest;
import com.ossanasur.cbconnect.module.finance.dto.request.RembourserPrefinancementRequest;
import com.ossanasur.cbconnect.module.finance.dto.response.CouvertureFinanciereResponse;
import com.ossanasur.cbconnect.module.finance.dto.response.PrefinancementDetailResponse;
import com.ossanasur.cbconnect.module.finance.dto.response.PrefinancementResponse;
import com.ossanasur.cbconnect.module.finance.dto.response.RemboursementSuggestionResponse;
import com.ossanasur.cbconnect.utils.DataResponse;

import java.util.List;
import java.util.UUID;

public interface PrefinancementService {

    DataResponse<PrefinancementDetailResponse> creer(
            PrefinancementCreateRequest request, String loginAuteur);

    DataResponse<PrefinancementDetailResponse> valider(
            UUID prefinancementTrackingId, String loginAuteur);

    DataResponse<PrefinancementDetailResponse> annuler(
            UUID prefinancementTrackingId,
            AnnulerPrefinancementRequest request,
            String loginAuteur);

    DataResponse<RemboursementSuggestionResponse> getRemboursementSuggere(
            UUID prefinancementTrackingId);

    DataResponse<PrefinancementDetailResponse> rembourser(
            UUID prefinancementTrackingId,
            RembourserPrefinancementRequest request,
            String loginAuteur);

    DataResponse<PrefinancementDetailResponse> getByTrackingId(UUID prefinancementTrackingId);

    DataResponse<List<PrefinancementResponse>> getBySinistre(UUID sinistreTrackingId);

    DataResponse<CouvertureFinanciereResponse> getCouvertureFinanciere(UUID sinistreTrackingId);
}
