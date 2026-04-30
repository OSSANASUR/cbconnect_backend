package com.ossanasur.cbconnect.module.finance.service;

import com.ossanasur.cbconnect.common.enums.StatutLotReglement;
import com.ossanasur.cbconnect.module.finance.dto.request.CreerLotRequest;
import com.ossanasur.cbconnect.module.finance.dto.request.SaisieComptableLotRequest;
import com.ossanasur.cbconnect.module.finance.dto.response.LotReglementResponse;
import com.ossanasur.cbconnect.module.finance.dto.response.SinistrePayableResponse;
import com.ossanasur.cbconnect.utils.DataResponse;
import com.ossanasur.cbconnect.utils.PaginatedResponse;

import java.util.List;
import java.util.UUID;

public interface LotReglementService {

    DataResponse<List<SinistrePayableResponse>> listerSinistresPayables(UUID expertTrackingId);

    DataResponse<LotReglementResponse> creerLot(CreerLotRequest req, String loginAuteur);

    DataResponse<LotReglementResponse> validerTechniqueLot(UUID lotTrackingId, String loginAuteur);

    DataResponse<LotReglementResponse> saisirComptableLot(
            UUID lotTrackingId, SaisieComptableLotRequest req, String loginAuteur);

    DataResponse<LotReglementResponse> validerComptableLot(UUID lotTrackingId, String loginAuteur);

    DataResponse<LotReglementResponse> getByTrackingId(UUID lotTrackingId);

    PaginatedResponse<LotReglementResponse> lister(
            UUID expertTrackingId, StatutLotReglement statut, int page, int size);
}
