package com.ossanasur.cbconnect.module.courrier.dto.response;
import com.ossanasur.cbconnect.common.enums.*;
import java.time.LocalDate; import java.time.LocalDateTime; import java.util.UUID;
public record CourrierResponse(
    UUID courrierTrackingId, String referenceCourrier, TypeCourrier typeCourrier,
    NatureCourrier nature, String expediteur, String destinataire, String objet,
    LocalDate dateCourrier, LocalDate dateReception, CanalCourrier canal,
    boolean traite, LocalDateTime dateTraitement, String sinistreNumeroLocal
) {}
