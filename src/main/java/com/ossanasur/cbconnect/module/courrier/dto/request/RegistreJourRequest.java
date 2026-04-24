package com.ossanasur.cbconnect.module.courrier.dto.request;

import com.ossanasur.cbconnect.common.enums.TypeRegistre;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** Création d'un registre journalier (1 par (date, type)). */
public record RegistreJourRequest(
    @NotNull LocalDate dateJour,
    @NotNull TypeRegistre typeRegistre
) {}
