package com.ossanasur.cbconnect.module.messagerie.dto.response;

public record PieceJointe(
        /** Index de la part MIME (pour le téléchargement) */
        int index,
        String nom,
        String contentType, // ex: "application/pdf", "image/jpeg"
        long taille, // en octets
        /** Contenu Base64 — null dans la liste, rempli à la demande */
        String contenuBase64) {
}
