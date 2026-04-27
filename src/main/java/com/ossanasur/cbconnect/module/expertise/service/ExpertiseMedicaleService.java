package com.ossanasur.cbconnect.module.expertise.service;

import com.ossanasur.cbconnect.module.expertise.dto.request.ExpertiseMedicaleRequest;
import com.ossanasur.cbconnect.module.expertise.dto.response.ExpertiseMedicaleResponse;
import com.ossanasur.cbconnect.utils.DataResponse;
import java.util.List;
import java.util.UUID;

public interface ExpertiseMedicaleService {
    DataResponse<ExpertiseMedicaleResponse> create(ExpertiseMedicaleRequest r, String loginAuteur);

    DataResponse<ExpertiseMedicaleResponse> update(UUID id, ExpertiseMedicaleRequest r, String loginAuteur);

    DataResponse<ExpertiseMedicaleResponse> getByTrackingId(UUID id);

    DataResponse<List<ExpertiseMedicaleResponse>> getByVictime(UUID victimeId);

    DataResponse<List<ExpertiseMedicaleResponse>> getEnAttente();

    DataResponse<Void> delete(UUID id, String loginAuteur);
}
