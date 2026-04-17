package com.ossanasur.cbconnect.module.ged.dto.request;
import com.ossanasur.cbconnect.common.enums.TypeDocumentPaperless;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
public record UploadDocumentRequest(
    @NotNull String titre,
    @NotNull TypeDocumentPaperless typeDocument,
    LocalDate dateDocument,
    UUID sinistreTrackingId,
    UUID victimeTrackingId,
    List<Integer> tagIds
) {}
