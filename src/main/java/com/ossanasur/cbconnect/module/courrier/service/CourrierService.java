package com.ossanasur.cbconnect.module.courrier.service;
import com.ossanasur.cbconnect.module.courrier.dto.request.CourrierRequest;
import com.ossanasur.cbconnect.module.courrier.dto.response.CourrierResponse;
import com.ossanasur.cbconnect.utils.DataResponse;
import java.util.List; import java.util.UUID;
public interface CourrierService {
    DataResponse<CourrierResponse> enregistrer(CourrierRequest r, String loginAuteur);
    DataResponse<CourrierResponse> getByTrackingId(UUID id);
    DataResponse<List<CourrierResponse>> getBySinistre(UUID sinistreId);
    DataResponse<List<CourrierResponse>> getNonTraites();
    DataResponse<List<CourrierResponse>> getAll();
    DataResponse<Void> marquerTraite(UUID id, String loginAuteur);
    DataResponse<Void> supprimer(UUID id, String loginAuteur);
}
