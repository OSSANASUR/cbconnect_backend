package com.ossanasur.cbconnect.module.auth.service;
import com.ossanasur.cbconnect.module.auth.dto.request.ProfilRequest;
import com.ossanasur.cbconnect.module.auth.dto.response.ProfilResponse;
import com.ossanasur.cbconnect.utils.DataResponse;
import com.ossanasur.cbconnect.utils.PaginatedResponse;
import java.util.List;
import java.util.UUID;
public interface ProfilService {
    DataResponse<ProfilResponse> create(ProfilRequest request, String loginAuteur);
    DataResponse<ProfilResponse> update(UUID trackingId, ProfilRequest request, String loginAuteur);
    DataResponse<ProfilResponse> getByTrackingId(UUID trackingId);
    DataResponse<List<ProfilResponse>> getAll();
    DataResponse<List<ProfilResponse>> getByOrganisme(UUID organismeTrackingId);
    DataResponse<Void> delete(UUID trackingId, String loginAuteur);
    PaginatedResponse<ProfilResponse> getHistory(UUID trackingId, int page, int size);
}
