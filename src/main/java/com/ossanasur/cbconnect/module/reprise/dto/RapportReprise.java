package com.ossanasur.cbconnect.module.reprise.dto;

import java.util.List;

/**
 * Rapport succinct retourné après chaque batch d'import.
 */
public record RapportReprise(
                int totalTraite,
                int importes,
                int doublons,
                int erreurs,
                List<DetailReprise> details) {
        public record DetailReprise(
                        String type, // IMPORT | DOUBLON | ERROR
                        String numero, // numeroSinistreManuel
                        String message) {
        }

        /** Factory — batch entier en erreur */
        public static RapportReprise batchError(int size, String msg) {
                return new RapportReprise(size, 0, 0, size,
                                List.of(new DetailReprise("ERROR", null, msg)));
        }
}
