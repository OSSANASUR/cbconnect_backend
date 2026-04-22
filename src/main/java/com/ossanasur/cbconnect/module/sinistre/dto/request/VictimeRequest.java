package com.ossanasur.cbconnect.module.sinistre.dto.request;

import com.ossanasur.cbconnect.common.enums.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record VictimeRequest(
    @NotBlank String nom, @NotBlank String prenoms, @NotNull LocalDate dateNaissance,
    @NotBlank String sexe, String nationalite,
    @NotNull StatutActivite statutActivite, BigDecimal revenuMensuel,
    UUID paysResidenceTrackingId, UUID sinistreTrackingId,

    /* ═══════════════ Extension wizard V22 ═══════════════ */
    TypeVictime typeVictime,
    Boolean estAdversaire,
    String profession,
    TypeDommage typeDommage,

    /* Adversaire : conducteur */
    String telephone,
    String numeroPermis,
    List<String> categoriesPermis,
    LocalDate dateDelivrance,
    String lieuDelivrance,

    /* Adversaire : véhicule */
    String marqueVehicule,
    String modeleVehicule,
    String genreVehicule,
    String couleurVehicule,
    String immatriculation,
    String numeroChassis,
    LocalDate prochaineVT,
    Integer capaciteVehicule,
    Integer nbPersonnesABord,
    String proprietaireVehicule,
    Boolean aRemorque,
    String assureurAdverse,

    /* Adversaire : dommages */
    String descriptionDegats,
    Integer blessesLegers, Integer blessesGraves, Integer deces
) {}
