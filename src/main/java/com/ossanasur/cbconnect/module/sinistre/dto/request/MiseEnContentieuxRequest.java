package com.ossanasur.cbconnect.module.sinistre.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

/** Passage d'un dossier en CONTENTIEUX (procédure judiciaire). */
public record MiseEnContentieuxRequest(
        @NotBlank String niveauJuridiction,  // ex : "Tribunal 1ère instance", "Cour d'appel", "Cassation"
        LocalDate dateProchaineAudience,
        String observations
) {}
