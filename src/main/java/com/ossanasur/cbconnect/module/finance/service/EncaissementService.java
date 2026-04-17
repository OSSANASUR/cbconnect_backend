package com.ossanasur.cbconnect.module.finance.service;
import com.ossanasur.cbconnect.module.finance.dto.request.EncaissementRequest;
import com.ossanasur.cbconnect.module.finance.dto.response.EncaissementResponse;
import com.ossanasur.cbconnect.utils.DataResponse;
import java.util.List; import java.util.UUID;
public interface EncaissementService {
    DataResponse<EncaissementResponse> create(EncaissementRequest r, String loginAuteur);
    DataResponse<EncaissementResponse> getByTrackingId(UUID id);
    DataResponse<List<EncaissementResponse>> getBySinistre(UUID sinistreId);
    DataResponse<Void> encaisser(UUID id, java.time.LocalDate dateEncaissement, String loginAuteur);
    DataResponse<Void> annuler(UUID id, String motif, String loginAuteur);
}
