package com.ossanasur.cbconnect.module.reclamation.dto.response;

import com.ossanasur.cbconnect.common.enums.TypeDommage;

import java.util.UUID;

public record TypePieceResponse(
        UUID trackingId,
        String libelle,
        TypeDommage typeDommage, // null = COMMUN
        String typeDommageLabel, // "Commun" | "Corporel" | "Matériel" | "Mixte"
        boolean obligatoire,
        int ordre,
        boolean actif) {
}