package com.ossanasur.cbconnect.module.auth.service;
import com.ossanasur.cbconnect.module.auth.dto.request.OrganismeRequest;
import com.ossanasur.cbconnect.module.auth.dto.response.OrganismeResponse;
import com.ossanasur.cbconnect.security.dto.response.TwoFactorStatusResponse;
import com.ossanasur.cbconnect.utils.DataResponse;
import com.ossanasur.cbconnect.utils.PaginatedResponse;
import java.util.List;
import java.util.UUID;
public interface OrganismeService {
    DataResponse<OrganismeResponse> create(OrganismeRequest request, String loginAuteur);
    DataResponse<OrganismeResponse> update(UUID trackingId, OrganismeRequest request, String loginAuteur);
    DataResponse<OrganismeResponse> getByTrackingId(UUID trackingId);
    DataResponse<List<OrganismeResponse>> getAll();
    DataResponse<List<OrganismeResponse>> getAllByType(com.ossanasur.cbconnect.common.enums.TypeOrganisme type);
    DataResponse<Void> delete(UUID trackingId, String loginAuteur);
    PaginatedResponse<OrganismeResponse> getHistory(UUID trackingId, int page, int size);
    DataResponse<TwoFactorStatusResponse> getTwoFactor(UUID trackingId);
    DataResponse<TwoFactorStatusResponse> updateTwoFactor(UUID trackingId, boolean enabled, String loginAuteur);
}
