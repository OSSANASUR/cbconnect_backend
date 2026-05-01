package com.ossanasur.cbconnect.module.finance.service;

import com.ossanasur.cbconnect.common.enums.TypeOperationFinanciere;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;

/**
 * Génère un numéro d'opération financière unique au format
 * <TYPE>-<YYYY>-<NUM_SINISTRE>-<SEQ> (cf. spec 2026-04-28).
 *
 * <p>Le compteur SEQ est par (sinistre, type) — RT et RC ont des compteurs
 * indépendants pour un même sinistre.
 *
 * <p>La méthode lit dans la table cible selon le type :
 *   - RT/RC/AN → table `paiement`
 *   - PF       → table `prefinancement`
 */
public interface NumeroOperationGenerator {

    /**
     * Génère un numéro d'opération unique pour le couple (type, sinistre).
     *
     * @throws IllegalStateException si le sinistre n'a ni numeroSinistreLocal
     *         ni numeroSinistreManuel
     */
    String genererNumero(TypeOperationFinanciere type, Sinistre sinistre);
}
