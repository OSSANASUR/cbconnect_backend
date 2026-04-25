package com.ossanasur.cbconnect.module.finance.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

public record ReglementComptableRequest(

        @NotBlank(message = "Le numéro de chèque est obligatoire") @Size(max = 100) String numeroChequeEmis,

        @Size(max = 150) String banqueCheque,

        @NotNull(message = "La date d'émission du règlement est obligatoire") @PastOrPresent(message = "La date d'émission ne peut pas être dans le futur") LocalDate dateEmissionReglement,

        @NotNull(message = "La date d'émission du chèque est obligatoire") @PastOrPresent LocalDate dateEmissionCheque

) {
}
