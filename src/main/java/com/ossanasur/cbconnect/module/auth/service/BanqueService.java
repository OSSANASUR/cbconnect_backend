package com.ossanasur.cbconnect.module.auth.service;

import com.ossanasur.cbconnect.module.auth.dto.request.BanqueRequest;
import com.ossanasur.cbconnect.module.auth.dto.response.BanqueResponse;
import com.ossanasur.cbconnect.utils.DataResponse;

import java.util.List;
import java.util.UUID;

public interface BanqueService {
    DataResponse<BanqueResponse> create(BanqueRequest request, String loginAuteur);
    DataResponse<BanqueResponse> update(UUID id, BanqueRequest request, String loginAuteur);
    DataResponse<BanqueResponse> getByTrackingId(UUID id);
    DataResponse<List<BanqueResponse>> getAll();
    DataResponse<Void> delete(UUID id, String loginAuteur);
}
