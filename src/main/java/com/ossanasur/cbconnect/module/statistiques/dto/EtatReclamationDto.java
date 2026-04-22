package com.ossanasur.cbconnect.module.statistiques.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * État de réclamation global — double vue (nombre + montant).
 *
 * Deux états :
 * togoVersHomologues : sinistres ET, groupés par pays_emetteur →
 * organisme_homologue
 * homologuesVersTogo : sinistres TE, groupés par pays_gestionnaire →
 * organisme_membre
 *
 * Le montant est PROPORTIONNEL au taux RC :
 * montantProportion = dossier.montantTotalReclame * sinistre.tauxRc / 100
 *
 * Seuls les dossiers NON clôturés sont inclus (statutReclamation != CLOTURE).
 *
 * Colonnes = statuts de réclamation :
 * BON_A_PAYER | ARBITRAGE | PROBLEME_RC | ATTENTE_OFFRE | ATTENTE_PIECES |
 * AUTRES
 */
public record EtatReclamationDto(

                String dateArrete, // date de production de l'état
                EtatReclamation togoVersHomologues,
                EtatReclamation homologuesVersTogo

) {

        // ── Un des deux états (double vue nb + montant) ───────────────
        public record EtatReclamation(
                        List<BlocPays> parPays,
                        TotalGlobal total) {
        }

        // ── Un bloc = un pays partenaire ─────────────────────────────
        public record BlocPays(
                        String pays,
                        String codePays,
                        List<LigneCompagnie> lignes,
                        LigneCompagnie totalPays) {
        }

        // ── Une ligne = une compagnie homologue ou membre ─────────────
        public record LigneCompagnie(
                        String compagnie,

                        // TOTAL (tous statuts non clôturés)
                        long nbTotal,
                        BigDecimal montantTotal,

                        // Par statut : nb + montant
                        Map<String, Long> nbParStatut, // clé = StatutReclamation.name()
                        Map<String, BigDecimal> montantParStatut) {
        }

        // ── Totaux globaux (tous pays) ─────────────────────────────────
        public record TotalGlobal(
                        long nbTotal,
                        BigDecimal montantTotal,
                        Map<String, Long> nbParStatut,
                        Map<String, BigDecimal> montantParStatut) {
        }
}
