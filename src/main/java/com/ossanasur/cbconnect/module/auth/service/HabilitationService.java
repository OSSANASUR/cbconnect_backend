package com.ossanasur.cbconnect.module.auth.service;

import com.ossanasur.cbconnect.module.auth.dto.request.HabilitationRequest;
import com.ossanasur.cbconnect.module.auth.dto.response.HabilitationResponse;
import com.ossanasur.cbconnect.utils.DataResponse;

import java.util.List;
import java.util.UUID;

public interface HabilitationService {
    DataResponse<HabilitationResponse> create(HabilitationRequest request, String loginAuteur);
    DataResponse<HabilitationResponse> update(UUID trackingId, HabilitationRequest request, String loginAuteur);
    DataResponse<HabilitationResponse> getByTrackingId(UUID trackingId);
    DataResponse<List<HabilitationResponse>> getAll();
    DataResponse<List<HabilitationResponse>> getByModule(UUID moduleTrackingId);
    DataResponse<Void> delete(UUID trackingId, String loginAuteur);
}
