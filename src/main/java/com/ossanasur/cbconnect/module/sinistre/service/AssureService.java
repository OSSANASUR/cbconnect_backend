package com.ossanasur.cbconnect.module.sinistre.service;

import com.ossanasur.cbconnect.module.sinistre.dto.request.AssureRequest;
import com.ossanasur.cbconnect.module.sinistre.dto.response.AssureResponse;
import com.ossanasur.cbconnect.utils.DataResponse;

import java.util.UUID;

public interface AssureService {
    DataResponse<AssureResponse> create(AssureRequest r, String loginAuteur);
    DataResponse<AssureResponse> update(UUID id, AssureRequest r, String loginAuteur);
    DataResponse<AssureResponse> getByTrackingId(UUID id);
    DataResponse<AssureResponse> getByNumeroAttestation(String numero);
    DataResponse<Void> delete(UUID id, String loginAuteur);
}
