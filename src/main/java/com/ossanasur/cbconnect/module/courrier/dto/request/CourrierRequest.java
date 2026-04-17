package com.ossanasur.cbconnect.module.courrier.dto.request;
import com.ossanasur.cbconnect.common.enums.*;
import jakarta.validation.constraints.NotBlank; import jakarta.validation.constraints.NotNull;
import java.time.LocalDate; import java.util.UUID;
public record CourrierRequest(
    String referenceCourrier, @NotNull TypeCourrier typeCourrier, @NotNull NatureCourrier nature,
    @NotBlank String expediteur, @NotBlank String destinataire, @NotBlank String objet,
    @NotNull LocalDate dateCourrier, LocalDate dateReception,
    CanalCourrier canal, String referenceBordereau,
    UUID sinistreTrackingId
) {}
