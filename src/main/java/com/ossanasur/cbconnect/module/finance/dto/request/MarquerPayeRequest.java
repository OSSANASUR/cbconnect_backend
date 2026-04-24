package com.ossanasur.cbconnect.module.finance.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

public record MarquerPayeRequest(

        @NotNull(message = "La date de paiement est obligatoire") @PastOrPresent(message = "La date de paiement ne peut pas être dans le futur") LocalDate datePaiement

) {
}