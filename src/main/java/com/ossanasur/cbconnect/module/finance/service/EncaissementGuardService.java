package com.ossanasur.cbconnect.module.finance.service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Garde-fou métier : pas de règlement sans encaissement préalable.
 * Voir docs/superpowers/specs/2026-04-27-encaissement-prerequis-reglement-design.md
 */
public interface EncaissementGuardService {

    /** Règle A : un encaissement non-annulé doit exister sur le sinistre. */
    void verifierRegleA(UUID sinistreTrackingId);

    /** Règle B : au moins un encaissement crédité en banque (ENCAISSE). */
    void verifierRegleB(UUID sinistreTrackingId);

    /**
     * Règle C : Σ encaissements ENCAISSE ≥ Σ règlements actifs + montantNouveau.
     * Pour validerComptable, montantNouveau = BigDecimal.ZERO (déjà compté).
     */
    void verifierRegleC(UUID sinistreTrackingId, BigDecimal montantNouveau);
}
