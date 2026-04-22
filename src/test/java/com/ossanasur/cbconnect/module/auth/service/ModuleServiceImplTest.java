package com.ossanasur.cbconnect.module.auth.service;

import com.ossanasur.cbconnect.module.auth.dto.response.ModuleResponse;
import com.ossanasur.cbconnect.module.auth.entity.ModuleEntity;
import com.ossanasur.cbconnect.module.auth.mapper.ModuleMapper;
import com.ossanasur.cbconnect.module.auth.repository.ModuleEntityRepository;
import com.ossanasur.cbconnect.module.auth.service.impl.ModuleServiceImpl;
import com.ossanasur.cbconnect.utils.DataResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModuleServiceImplTest {
    @Mock ModuleEntityRepository repo;
    @Mock ModuleMapper mapper;
    @InjectMocks ModuleServiceImpl service;

    @Test
    void getAll_returnsActiveModulesMapped() {
        ModuleEntity m = ModuleEntity.builder()
            .moduleTrackingId(UUID.randomUUID()).nomModule("SINISTRES").actif(true).build();
        when(repo.findAllActifs()).thenReturn(List.of(m));
        when(mapper.toResponse(m)).thenReturn(
            new ModuleResponse(m.getModuleTrackingId(), "SINISTRES", null, true));

        DataResponse<List<ModuleResponse>> res = service.getAll();

        assertThat(res.isSuccess()).isTrue();
        assertThat(res.getData()).hasSize(1);
        assertThat(res.getData().get(0).nomModule()).isEqualTo("SINISTRES");
    }
}
