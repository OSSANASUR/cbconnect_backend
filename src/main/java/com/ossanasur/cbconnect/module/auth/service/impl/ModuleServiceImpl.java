package com.ossanasur.cbconnect.module.auth.service.impl;

import com.ossanasur.cbconnect.module.auth.dto.response.ModuleResponse;
import com.ossanasur.cbconnect.module.auth.mapper.ModuleMapper;
import com.ossanasur.cbconnect.module.auth.repository.ModuleEntityRepository;
import com.ossanasur.cbconnect.module.auth.service.ModuleService;
import com.ossanasur.cbconnect.utils.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModuleServiceImpl implements ModuleService {
    private final ModuleEntityRepository repository;
    private final ModuleMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<ModuleResponse>> getAll() {
        List<ModuleResponse> list = repository.findAllActifs().stream()
            .map(mapper::toResponse)
            .collect(Collectors.toList());
        return DataResponse.success(list);
    }
}
