package com.ossanasur.cbconnect.common.enums;

/**
 * Statut de réclamation par dossier victime.
 *
 * Distinct de StatutDossierReclamation (workflow interne).
 * Permet de suivre l'état de chaque victime indépendamment
 * du statut global du sinistre.
 *
 * Correspond aux colonnes de l'état R5 :
 * BON_A_PAYER → "BON À PAYER (1)"
 * ARBITRAGE → "ARBITRAGE (2)"
 * PROBLEME_RC → "PROBLÈME RC (3)"
 * ATTENTE_OFFRE → "ATTENTE OFFRE (4)"
 * ATTENTE_PIECES → "ATTENTE PIÈCES (5)"
 * AUTRES → "Autres"
 * CLOTURE → dossier clos (exclu des états de réclamation)
 */
public enum StatutReclamation {
    BON_A_PAYER,
    ARBITRAGE,
    PROBLEME_RC,
    ATTENTE_OFFRE,
    ATTENTE_PIECES,
    AUTRES,
    CLOTURE
}
