package com.ossanasur.cbconnect.common.enums;

/**
 * Cycle de vie d'un bordereau de transmission de courriers.
 *
 * BROUILLON            : en préparation, modifiable
 * IMPRIME              : bordereau imprimé, remis au coursier, plus modifiable
 * REMIS_TRANSPORTEUR   : déposé à la poste / bus / DHL (facture transporteur reçue)
 * DECHARGE_RECUE       : décharge signée du bureau homologue retournée et archivée
 * RETOURNE             : bordereau retourné sans remise (destinataire absent, etc.)
 */
public enum StatutBordereau {
    BROUILLON,
    IMPRIME,
    REMIS_TRANSPORTEUR,
    DECHARGE_RECUE,
    RETOURNE
}
