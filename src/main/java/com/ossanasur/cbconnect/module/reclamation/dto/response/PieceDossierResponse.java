package com.ossanasur.cbconnect.module.reclamation.dto.response;

import com.ossanasur.cbconnect.common.enums.StatutPiece;
import com.ossanasur.cbconnect.common.enums.TypeDommage;

import java.time.LocalDate;
import java.util.UUID;

public record PieceDossierResponse(
                UUID trackingId,
                // Type de pièce
                UUID typePieceTrackingId,
                String typePieceLibelle,
                TypeDommage typeDommage,
                boolean obligatoire,
                int ordre,
                // Statut
                StatutPiece statut,
                LocalDate dateReception,
                String notes,
                // Document GED associé (null si ATTENDUE)
                UUID ossanGedDocumentTrackingId,
                String ossanGedDocumentTitre,
                Integer ossanGedDocumentId // ID OssanGed pour prévisualisation
) {
}