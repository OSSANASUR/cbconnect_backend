package com.ossanasur.cbconnect.module.sinistre.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.UUID;

public record AssureRequest(
    @NotBlank String nomAssure,
    String prenomAssure,
    @NotBlank String nomComplet,
    String numeroPolice,
    String numeroAttestation,
    String numeroCGrise,
    String proprietaireVehicule,
    String immatriculation,
    String marqueVehicule,
    String telephone,
    String adresse,
    UUID organismeTrackingId,

    /* ── Extension wizard V22 ── */
    Boolean estPersonneMorale,
    String profession,
    LocalDate prochaineVT,
    Integer capaciteVehicule,
    Integer nbPersonnesABord,
    Boolean aRemorque
) {}
