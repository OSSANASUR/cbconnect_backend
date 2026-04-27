package com.ossanasur.cbconnect.module.sinistre.dto.response;

import java.math.BigDecimal;

/**
 * Synthèse encaissement vs règlements pour un sinistre — pilote l'UX du frontend.
 * Voir spec 2026-04-27-encaissement-prerequis-reglement-design.md §4.4.
 */
public record EncaissementStatusResponse(
        boolean aAuMoinsUnEncaissement,
        boolean aAuMoinsUnEncaisse,
        BigDecimal totalEncaisse,
        BigDecimal totalEngage,
        BigDecimal couverture,
        boolean regleAOk,
        boolean regleBOk,
        boolean regleCOk,
        String messageBlocant) {
}
