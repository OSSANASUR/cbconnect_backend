package com.ossanasur.cbconnect.module.sinistre.service;
import com.ossanasur.cbconnect.module.sinistre.dto.request.VictimeRequest;
import com.ossanasur.cbconnect.module.sinistre.dto.response.VictimeResponse;
import com.ossanasur.cbconnect.utils.DataResponse;
import java.util.List; import java.util.UUID;
public interface VictimeService {
    DataResponse<VictimeResponse> create(VictimeRequest r, String loginAuteur);
    DataResponse<VictimeResponse> update(UUID id, VictimeRequest r, String loginAuteur);
    DataResponse<VictimeResponse> getByTrackingId(UUID id);
    DataResponse<List<VictimeResponse>> getBySinistre(UUID sinistreId);
    DataResponse<Void> delete(UUID id, String loginAuteur);
    DataResponse<Void> changerStatutVictime(UUID id, String statut, String loginAuteur);
}
