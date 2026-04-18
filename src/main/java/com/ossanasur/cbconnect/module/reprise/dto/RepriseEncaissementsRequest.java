package com.ossanasur.cbconnect.module.reprise.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

/**
 * Requête d'import batch encaissements historiques.
 * Envoyée par le frontend après parsing du fichier Excel BNCB.
 *
 * Mapping colonnes Excel → champs :
 * Col B DATE RECEPTION → dateReception
 * Col C DATE SINISTRE → dateSinistre (pour sinistre minimal)
 * Col D N° BNCB TG → numeroSinistreManuel
 * Col E N° BNCB HOMOLOGUE → numeroSinistreHomologue
 * Col F PAYEUR → organismeEmetteurCode
 * Col G ASSURES → assureNomComplet (sinistre minimal)
 * Col H MODE DE PAIEMENT → modePaiement (BANQUE | VIREMENT)
 * Col I BANQUE → banqueEmettrice
 * Col J N° CHEQUE OU VIREMENT → numeroCheque
 * Col K LIEU DE SURVENANCE → lieuSurvenance (E | T)
 * Col M PRINCIPAL ENCAISSE → montantTheorique
 * Col N FG ENCAISSE → produitFraisGestion
 * Col P PG REELLEMENT ENCAISSE → pgEncaisse (ajouté à Col M → montantCheque)
 */
public record RepriseEncaissementsRequest(

        @NotEmpty(message = "La liste des encaissements est obligatoire") @Size(max = 100, message = "Maximum 100 encaissements par batch") List<EncaissementRepriseDto> encaissements

) {

    public record EncaissementRepriseDto(

            /** N° BNCB TG — permet de retrouver le sinistre en base */
            String numeroSinistreManuel,

            /** N° BNCB Homologue (col E) — conservé pour traçabilité */
            String numeroSinistreHomologue,

            /** Col B — date de réception du chèque */
            String dateReception,

            /** Col C — date de l'accident (pour créer le sinistre minimal si absent) */
            String dateSinistre,

            /** Col F — code de l'organisme payeur (ex: "BNCB BF", "FIDELIA TG") */
            String organismeEmetteurCode,

            /** Col G — nom de l'assuré (pour sinistre minimal) */
            String assureNomComplet,

            /** Col H — BANQUE | VIREMENT */
            String modePaiement,

            /** Col I — nom de la banque */
            String banqueEmettrice,

            /** Col J — numéro de chèque ou référence virement */
            String numeroCheque,

            /**
             * Col K — lieu de survenance.
             * "T" = Togo → SURVENU_TOGO | "E" = Étranger → SURVENU_ETRANGER
             * Utilisé uniquement pour la création du sinistre minimal.
             */
            String lieuSurvenance,

            /** Col M — PRINCIPAL ENCAISSE → montantTheorique */
            BigDecimal montantTheorique,

            /** Col N — FG ENCAISSE → produitFraisGestion */
            BigDecimal produitFraisGestion,

            /**
             * Col P — PG REELLEMENT ENCAISSE.
             * montantCheque = montantTheorique + pgEncaisse
             */
            BigDecimal pgEncaisse

    ) {
    }
}
