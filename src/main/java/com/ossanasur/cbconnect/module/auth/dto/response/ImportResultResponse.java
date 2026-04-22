package com.ossanasur.cbconnect.module.auth.dto.response;

import java.util.List;

public record ImportResultResponse(int totalCrees, List<ImportErrorResponse> erreurs) {}
