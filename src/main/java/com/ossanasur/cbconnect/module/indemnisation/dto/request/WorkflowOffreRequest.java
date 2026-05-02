package com.ossanasur.cbconnect.module.indemnisation.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record WorkflowOffreRequest(
    LocalDate dateEnvoiHomologue,
    LocalDate dateReponseHomologue,
    BigDecimal montantContreOffre,
    String descriptionContreOffre,
    String ossanGedDocumentIdContreOffre,
    LocalDate dateEnvoiVictime,
    LocalDate dateAccordVictime,
    String observationsAccord,
    String ossanGedDocumentIdAccord,
    LocalDate dateRejetVictime
) {}
