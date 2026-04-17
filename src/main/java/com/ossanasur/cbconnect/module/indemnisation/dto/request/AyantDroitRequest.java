package com.ossanasur.cbconnect.module.indemnisation.dto.request;
import com.ossanasur.cbconnect.common.enums.LienParente;
import jakarta.validation.constraints.NotBlank; import jakarta.validation.constraints.NotNull;
import java.time.LocalDate; import java.util.UUID;
public record AyantDroitRequest(
    @NotBlank String nom, @NotBlank String prenoms,
    @NotNull LocalDate dateNaissance, @NotBlank String sexe,
    @NotNull LienParente lien, boolean estOrphelinDouble, boolean poursuiteEtudes,
    @NotNull UUID victimeTrackingId
) {}
