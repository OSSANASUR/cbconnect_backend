package com.ossanasur.cbconnect.module.expertise.service;
import com.ossanasur.cbconnect.common.enums.TypeExpert;
import com.ossanasur.cbconnect.module.expertise.dto.request.ExpertRequest;
import com.ossanasur.cbconnect.module.expertise.dto.response.ExpertResponse;
import com.ossanasur.cbconnect.utils.DataResponse;
import java.util.List; import java.util.UUID;
public interface ExpertService {
    DataResponse<ExpertResponse> create(ExpertRequest r, String loginAuteur);
    DataResponse<ExpertResponse> update(UUID id, ExpertRequest r, String loginAuteur);
    DataResponse<ExpertResponse> getByTrackingId(UUID id);
    DataResponse<List<ExpertResponse>> getAllActifsByType(TypeExpert type);
    DataResponse<Void> delete(UUID id, String loginAuteur);
}
