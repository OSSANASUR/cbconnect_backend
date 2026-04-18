package com.ossanasur.cbconnect.module.reclamation.dto.response;

import java.util.List;

public record MaturiteDossierResponse(
        String numeroDossier,
        boolean estMur,
        long nbPiecesRequises,
        long nbPiecesRecues,
        long nbPiecesAttendues,
        long nbPiecesRejetees,
        List<PieceDossierResponse> pieces) {
}