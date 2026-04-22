package com.ossanasur.cbconnect.module.auth.service;

import com.ossanasur.cbconnect.module.auth.dto.response.ModuleResponse;
import com.ossanasur.cbconnect.utils.DataResponse;
import java.util.List;

public interface ModuleService {
    DataResponse<List<ModuleResponse>> getAll();
}
