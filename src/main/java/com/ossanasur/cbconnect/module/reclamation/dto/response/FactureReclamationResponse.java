package com.ossanasur.cbconnect.module.reclamation.dto.response;
import com.ossanasur.cbconnect.common.enums.*;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.UUID;
public record FactureReclamationResponse(
    UUID factureTrackingId, String numeroFactureOriginal, TypeDepenseReclamation typeDepense,
    String nomPrestataire, LocalDate dateFacture, BigDecimal montantReclame, BigDecimal montantRetenu,
    StatutTraitementFacture statutTraitement, String motifRejet, boolean lienAvecAccidentVerifie,
    LocalDate dateTraitement, Integer ossanGedDocumentId
) {}
