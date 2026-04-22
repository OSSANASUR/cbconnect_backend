package com.ossanasur.cbconnect.module.sinistre.dto.response;

import com.ossanasur.cbconnect.common.enums.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record VictimeResponse(
    UUID victimeTrackingId,
    String nom, String prenoms, LocalDate dateNaissance, String sexe,
    String nationalite,
    TypeVictime typeVictime, StatutVictime statutVictime, StatutActivite statutActivite,
    BigDecimal revenuMensuel,
    boolean estDcdSuiteBlessures, LocalDate dateDeces, boolean lienDecesAccident,
    String paysResidenceLibelle,
    UUID sinistreTrackingId,

    /* ═══════════════ Extension wizard V22 ═══════════════ */
    boolean estAdversaire,
    String profession,
    TypeDommage typeDommage,
    String telephone,
    String numeroPermis,
    String categoriesPermis,
    LocalDate dateDelivrance,
    String lieuDelivrance,
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
    String descriptionDegats,
    Integer blessesLegers, Integer blessesGraves, Integer deces,

    /* ═══════════════ Négociation RC V27 (adversaires uniquement) ═══════════════ */
    PositionRc positionRc,
    Integer pourcentageRcPropose,
    String motifRejetRc,
    Integer nombreToursRc,
    Integer pourcentageRcFinal,
    LocalDateTime dateDerniereActionRc
) {}
