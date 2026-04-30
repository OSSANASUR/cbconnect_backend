package com.ossanasur.cbconnect.module.sinistre.dto.response;

import java.util.List;

public record EntiteConstatImportResponse(
    int totalLignes,
    int totalCrees,
    int totalIgnores,
    List<String> erreurs
) {}
