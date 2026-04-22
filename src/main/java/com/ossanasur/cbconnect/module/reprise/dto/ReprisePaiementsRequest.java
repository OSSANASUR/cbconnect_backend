package com.ossanasur.cbconnect.module.reprise.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * Requête d'import batch paiements historiques.
 *
 * Mapping colonnes Excel → champs :
 * Col B DATE PAIEMENT → datePaiement
 * Col C DATE SINISTRE → dateSinistre
 * Col D N° BNCB TG → numeroSinistreManuel
 * Col E N° BNCB HOMOLOGUE → numeroSinistreHomologue
 * Col F PAYEUR → organismePayeurCode (= émetteur encaissement)
 * Col G ASSURES → assureNomComplet
 * Col H DATE ENCAISSEMENT → dateEncaissement
 * Col I N°CHEQUE/VIREMENT ENCAISSÉ → numeroChequeEncaissement (lien vers
 * Encaissement)
 * Col J MONTANT TOTAL ENCAISSE → montantEncaisse (info contextuelle)
 * Col K BENEFICIAIRE → beneficiaire (nom brut → Victime minimale)
 * Col L MODE DE PAIEMENT → modePaiement (CHEQUE | VIREMENT)
 * Col M N°CHEQUE/VIREMENT PAIEMENT → numeroChequeEmis
 * Col N PRINCIPAL PAYE → principalPaye
 * Col O FG PAYE → fgPaye
 * Col P TOTAL PAYE → montant (= N + O)
 */
public record ReprisePaiementsRequest(

        @NotEmpty(message = "La liste des paiements est obligatoire") @Size(max = 100, message = "Maximum 100 paiements par batch") List<PaiementRepriseDto> paiements

) {

    public record PaiementRepriseDto(

            /** Col B — date du paiement */
            String datePaiement,

            /** Col C — date sinistre (pour sinistre/victime minimal si absent) */
            String dateSinistre,

            /** Col D — numéro sinistre BNCB TG */
            String numeroSinistreManuel,

            /** Col E — numéro sinistre homologue */
            String numeroSinistreHomologue,

            /** Col F — code organisme payeur (= émetteur de l'encaissement) */
            String organismePayeurCode,

            /** Col G — nom de l'assuré (pour sinistre minimal) */
            String assureNomComplet,

            /** Col H — date de l'encaissement lié */
            String dateEncaissement,

            /**
             * Col I — N° chèque ou virement de l'encaissement.
             * Permet de retrouver l'Encaissement en base.
             */
            String numeroChequeEncaissement,

            /** Col J — montant total encaissé (info contextuelle) */
            BigDecimal montantEncaisse,

            /**
             * Col K — nom du bénéficiaire.
             * Peut être une personne physique ou un organisme.
             * → Victime minimale créée si personne physique non trouvée.
             */
            String beneficiaire,

            /** Col L — CHEQUE | VIREMENT */
            String modePaiement,

            /** Col M — N° chèque ou virement émis pour ce paiement */
            String numeroChequeEmis,

            /** Col N — principal payé */
            BigDecimal principalPaye,

            /** Col O — frais de gestion payés */
            BigDecimal fgPaye,

            /**
             * Lieu de survenance (optionnel, déduit côté frontend depuis le
             * format du numeroSinistreManuel) :
             *   "T" → SURVENU_TOGO (ET : étranger en Togo)
             *   "E" → SURVENU_ETRANGER (TE : Togolais à l'étranger)
             * Si null/absent, le backend déduit depuis le format du numéro.
             */
            String lieuSurvenance

    ) {
    }
}
