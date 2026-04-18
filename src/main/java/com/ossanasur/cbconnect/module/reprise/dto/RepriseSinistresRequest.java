package com.ossanasur.cbconnect.module.reprise.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Requête d'import batch sinistres historiques.
 * Envoyée par le frontend après parsing Excel (SheetJS).
 * Maximum 100 sinistres par batch.
 */
public record RepriseSinistresRequest(
                @NotEmpty(message = "La liste des sinistres est obligatoire") @Size(max = 100, message = "Maximum 100 sinistres par batch") List<SinistreRepriseDto> sinistres) {

        /**
         * Un sinistre historique tel que parsé depuis le fichier Excel BNCB.
         *
         * Format des numéros :
         * ET → numeroSinistreManuel = "2025/001/BJ" (3 chiffres)
         * numeroSinistreLocal = "2025/0001/BJ" (4 chiffres)
         * TE → numeroSinistreManuel = "SU/001/BF/2025"
         * numeroSinistreLocal = "SU/0001/BF/2025"
         */
        public record SinistreRepriseDto(

                        /** Ex: "2025/001/BJ" (ET) ou "SU/001/BF/2025" (TE) */
                        String numeroSinistreManuel,

                        /** Même que manuel avec séquence sur 4 digits */
                        String numeroSinistreLocal,

                        /** Colonne F (ET) ou G (TE) du fichier Excel — ex: "BEN/2025-1/36828/869" */
                        String numeroSinistreEcarteBrune,

                        /** Colonne G (ET) ou H (TE) — ex: "2025/7/001" */
                        String numeroSinistreHomologue,

                        /** Format ISO: "2025-01-03" */
                        String dateAccident,

                        /** SURVENU_TOGO (ET) ou SURVENU_ETRANGER (TE) */
                        String typeSinistre,

                        /** MATERIEL | CORPOREL | MIXTE — mappé depuis DCM/DM/DC */
                        String typeDommage,

                        /** Colonne ASSURÉ */
                        String assureNomComplet,

                        /** Colonne IMMATRICULATION */
                        String assureImmatriculation,

                        /** Colonne ASSUREUR DECLARANT — ex: "SUNU BJ" */
                        String assureurDeclarant,

                        /** Colonne N°SIN COMPAGNIES */
                        String numeroPoliceAssureur,

                        /** Code pays ISO 2 lettres: BJ, BF, NE, TG, CI, SN, ML, GH... */
                        String paysEmetteurCode,

                        /** Code pays gestionnaire (colonne GESTIONNAIRE) */
                        String paysGestionnaireCode

        ) {
        }
}
