package com.ossanasur.cbconnect.module.auth.service;

import com.ossanasur.cbconnect.common.enums.ActionHabilitation;
import com.ossanasur.cbconnect.common.enums.TypeAccesHabilitation;
import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.module.auth.dto.request.HabilitationRequest;
import com.ossanasur.cbconnect.module.auth.dto.response.HabilitationResponse;
import com.ossanasur.cbconnect.module.auth.entity.ModuleEntity;
import com.ossanasur.cbconnect.module.auth.mapper.HabilitationMapper;
import com.ossanasur.cbconnect.module.auth.repository.ModuleEntityRepository;
import com.ossanasur.cbconnect.module.auth.service.impl.HabilitationServiceImpl;
import com.ossanasur.cbconnect.security.entity.Habilitation;
import com.ossanasur.cbconnect.security.repository.HabilitationRepository;
import com.ossanasur.cbconnect.utils.DataResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HabilitationServiceImplTest {
    @Mock HabilitationRepository habRepo;
    @Mock ModuleEntityRepository modRepo;
    @Mock HabilitationMapper mapper;
    @InjectMocks HabilitationServiceImpl service;

    @Test
    void create_persistsAndReturnsResponse() {
        UUID modId = UUID.randomUUID();
        ModuleEntity mod = ModuleEntity.builder().moduleTrackingId(modId).nomModule("SINISTRES").actif(true).build();
        HabilitationRequest req = new HabilitationRequest(
            "SINISTRES_LIRE", "Lire sinistres", "desc",
            ActionHabilitation.READ, TypeAccesHabilitation.ORGANISME, modId);
        when(habRepo.findActiveByCode("SINISTRES_LIRE")).thenReturn(Optional.empty());
        when(modRepo.findActiveByTrackingId(modId)).thenReturn(Optional.of(mod));
        when(habRepo.save(any(Habilitation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any())).thenReturn(
            new HabilitationResponse(UUID.randomUUID(), "SINISTRES_LIRE", "Lire sinistres",
                "desc", ActionHabilitation.READ, TypeAccesHabilitation.ORGANISME, "SINISTRES", modId));

        DataResponse<HabilitationResponse> res = service.create(req, "admin@x");

        assertThat(res.isSuccess()).isTrue();
        assertThat(res.getCode()).isEqualTo(201);
        ArgumentCaptor<Habilitation> captor = ArgumentCaptor.forClass(Habilitation.class);
        org.mockito.Mockito.verify(habRepo).save(captor.capture());
        assertThat(captor.getValue().getCreatedBy()).isEqualTo("admin@x");
        assertThat(captor.getValue().isActiveData()).isTrue();
    }

    @Test
    void create_rejectsDuplicateCode() {
        UUID modId = UUID.randomUUID();
        HabilitationRequest req = new HabilitationRequest(
            "X", "x", null, ActionHabilitation.READ, TypeAccesHabilitation.ORGANISME, modId);
        when(habRepo.findActiveByCode("X")).thenReturn(Optional.of(new Habilitation()));

        assertThatThrownBy(() -> service.create(req, "admin"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("existe deja");
    }

    @Test
    void create_missingModule_throws() {
        UUID modId = UUID.randomUUID();
        HabilitationRequest req = new HabilitationRequest(
            "X", "x", null, ActionHabilitation.READ, TypeAccesHabilitation.ORGANISME, modId);
        when(habRepo.findActiveByCode("X")).thenReturn(Optional.empty());
        when(modRepo.findActiveByTrackingId(modId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(req, "admin"))
            .isInstanceOf(RessourceNotFoundException.class);
    }
}
