package com.ossanasur.cbconnect.module.ged.dto.request;
import com.ossanasur.cbconnect.common.enums.TypeDocumentOssanGed;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
public record UploadDocumentRequest(
    @NotNull String titre,
    @NotNull TypeDocumentOssanGed typeDocument,
    LocalDate dateDocument,
    UUID sinistreTrackingId,
    UUID victimeTrackingId,
    UUID dossierReclamationTrackingId,
    List<Integer> tagIds,
    List<String> tagsExtra
) {}
