package com.ossanasur.cbconnect.module.comptabilite.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Event publié à chaque création/contre-passation d'imputation paiement→encaissement.
 * Un listener dédié (à venir dans une session brainstorming dédiée) générera les
 * EcritureComptable correspondantes selon le plan comptable de l'organisme.
 */
@Getter
@AllArgsConstructor
public class EcritureComptableEvent {

    public enum Type {
        ENGAGEMENT,        // Création d'imputation positive (RT)
        PAIEMENT,          // Validation RC (futur)
        CONTRE_PASSATION   // Annulation (imputation négative)
    }

    private final Type type;
    private final UUID paiementTrackingId;
    private final UUID encaissementTrackingId;
    private final BigDecimal montant;
    private final String libelle;
    private final String auteur;
}
