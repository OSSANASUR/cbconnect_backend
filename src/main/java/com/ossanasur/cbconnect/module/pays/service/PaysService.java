package com.ossanasur.cbconnect.module.pays.service;
import com.ossanasur.cbconnect.module.pays.dto.request.PaysRequest;
import com.ossanasur.cbconnect.module.pays.dto.response.PaysResponse;
import com.ossanasur.cbconnect.utils.DataResponse;
import java.util.List; import java.util.UUID;
public interface PaysService {
    DataResponse<PaysResponse> create(PaysRequest r, String loginAuteur);
    DataResponse<PaysResponse> update(UUID id, PaysRequest r, String loginAuteur);
    DataResponse<PaysResponse> getByTrackingId(UUID id);
    DataResponse<PaysResponse> getByCodeIso(String code);
    DataResponse<List<PaysResponse>> getAll();
    DataResponse<Void> delete(UUID id, String loginAuteur);
}
