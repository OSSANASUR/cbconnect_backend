package com.ossanasur.cbconnect.module.finance.service;

import java.util.List;
import java.util.UUID;

import com.ossanasur.cbconnect.common.enums.TypeMotif;
import com.ossanasur.cbconnect.module.finance.dto.request.ParamMotifRequest;
import com.ossanasur.cbconnect.module.finance.dto.response.ParamMotifResponse;
import com.ossanasur.cbconnect.utils.DataResponse;
import com.ossanasur.cbconnect.utils.PaginatedResponse;

public interface ParamMotifService {

    List<ParamMotifResponse> listerParType(TypeMotif type);

    PaginatedResponse<ParamMotifResponse> listerTous(int page, int size);

    ParamMotifResponse getByTrackingId(UUID trackingId);

    DataResponse<ParamMotifResponse> creer(ParamMotifRequest request, String login);

    DataResponse<ParamMotifResponse> modifier(UUID trackingId, ParamMotifRequest request, String login);

    DataResponse<Void> supprimer(UUID trackingId, String login);
}
