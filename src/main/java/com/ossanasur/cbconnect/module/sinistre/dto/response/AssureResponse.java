package com.ossanasur.cbconnect.module.sinistre.dto.response;

import java.time.LocalDate;
import java.util.UUID;

public record AssureResponse(
        UUID assureTrackingId,
        String nomAssure,
        String prenomAssure,
        String nomComplet,
        String numeroPolice,
        String numeroAttestation,
        String numeroCGrise,
        String proprietaireVehicule,
        String immatriculation,
        String marqueVehicule,
        String telephone,
        String adresse,
        UUID organismeTrackingId,
        String organismeRaisonSociale,
        String organismeCode,
        boolean estPersonneMorale,

        /* ── Extension wizard V22 ── */
        String profession,
        LocalDate prochaineVT,
        Integer capaciteVehicule,
        Integer nbPersonnesABord,
        boolean aRemorque
) {}
