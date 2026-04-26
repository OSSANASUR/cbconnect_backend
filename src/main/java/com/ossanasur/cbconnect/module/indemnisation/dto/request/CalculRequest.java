package com.ossanasur.cbconnect.module.indemnisation.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

/**
 * Paramètres optionnels saisis par l'instructeur avant le déclenchement du
 * calcul CIMA.
 *
 * Tous les champs sont optionnels (null = valeur calculée automatiquement ou
 * issue de la DB).
 *
 * Blessé → fraisMedicaux : montant retenu (art. 258)
 * Décès → fraisFuneraires : montant retenu (art. 264)
 * Commun → tauxRcOverride : si non null, remplace sinistre.tauxRc pour ce
 * calcul
 */
public record CalculRequest(

        /**
         * Art. 258 – Frais médicaux retenus (blessé).
         * Si null : somme des montants retenus dans les dossiers de réclamation.
         */
        BigDecimal fraisMedicaux,

        /**
         * Art. 264 – Frais funéraires retenus (décès).
         * Si null : valeur réglementaire CIMA = 3 × SMIG mensuel retenu.
         */
        BigDecimal fraisFuneraires,

        /**
         * Taux de responsabilité (0-100 %).
         * Si null : valeur de sinistre.tauxRc. Si toujours null → 100 % appliqué.
         */
        @DecimalMin("0") @DecimalMax("100") BigDecimal tauxRcOverride

) {
}