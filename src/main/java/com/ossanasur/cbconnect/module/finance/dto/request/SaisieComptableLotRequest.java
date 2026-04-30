package com.ossanasur.cbconnect.module.finance.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record SaisieComptableLotRequest(
        @NotBlank @Size(max = 30) String numeroChequeEmis,
        @NotBlank @Size(max = 150) String banqueCheque,
        @NotNull LocalDate dateEmissionCheque
) {}
