package com.ossanasur.cbconnect.module.finance.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

import com.ossanasur.cbconnect.common.enums.CategorieReglement;
import com.ossanasur.cbconnect.common.enums.StatutPaiement;
import com.ossanasur.cbconnect.common.enums.TypeOperationFinanciere;

public record PaiementDetailResponse(

                UUID paiementTrackingId,
                String numeroPaiement,
                TypeOperationFinanciere typeOperation,
                UUID sinistreTrackingId,
                String sinistreReference,

                String beneficiaire,
                BeneficiaireInfo beneficiaireDetail,

                BigDecimal montant,
                String modePaiement,
                String numeroChequeEmis,
                String banqueCheque,

                LocalDate dateEmission,
                LocalDate datePaiement,

                StatutPaiement statut,
                String motifAnnulation,
                String annuleParLogin,

                EcritureInfo ecritureComptable,
                List<EncaissementLieInfo> encaissements,

                boolean repriseHistorique,
                boolean excel,

                LocalDateTime createdAt,
                String createdBy,
                LocalDateTime updatedAt,
                String updatedBy,
                LocalDateTime deletedAt,
                String deletedBy,
                String parentCodeId,
                // V2026042601
                java.time.LocalDate dateEmissionCheque,
                com.ossanasur.cbconnect.common.enums.TypePrejudice typePrejudice,
                String motifComplement,

                CategorieReglement categorie,
                String motif,
                String beneficiaireExpertNom,
                UUID beneficiaireExpertTrackingId

) {

        public record BeneficiaireInfo(
                        String type,
                        UUID trackingId,
                        String nom,
                        String code,
                        String typeOrganisme,
                        String codePaysBCB) {
                /** Fabrique une instance bénéficiaire-victime. */
                public static BeneficiaireInfo ofVictime(UUID trackingId, String nom) {
                        return new BeneficiaireInfo("VICTIME", trackingId, nom, null, null, null);
                }

                /** Fabrique une instance bénéficiaire-organisme. */
                public static BeneficiaireInfo ofOrganisme(
                                UUID trackingId, String raisonSociale,
                                String code, String typeOrganisme, String codePaysBCB) {
                        return new BeneficiaireInfo("ORGANISME", trackingId, raisonSociale,
                                        code, typeOrganisme, codePaysBCB);
                }

                /** Fabrique une instance bénéficiaire-expert. */
                public static BeneficiaireInfo ofExpert(UUID trackingId, String nomComplet) {
                        return new BeneficiaireInfo("EXPERT", trackingId, nomComplet, null, null, null);
                }
        }

        public record EcritureInfo(
                        UUID ecritureTrackingId,
                        String numeroEcriture,
                        String libelle,
                        String statut,
                        BigDecimal montantTotal,
                        LocalDate dateEcriture) {
        }

        /**
         * Résumé d'un encaissement relié au paiement (relation ManyToMany).
         *
         * @param encaissementTrackingId UUID métier de l'encaissement.
         * @param numeroCheque           Numéro du chèque reçu.
         * @param montantCheque          Montant encaissé.
         * @param dateEncaissement       Date de réception des fonds.
         * @param statutCheque           Statut courant du chèque (texte brut).
         */
        public record EncaissementLieInfo(
                        UUID encaissementTrackingId,
                        String numeroCheque,
                        BigDecimal montantCheque,
                        LocalDate dateEncaissement,
                        String statutCheque) {
        }
}
