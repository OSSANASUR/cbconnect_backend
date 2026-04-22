package com.ossanasur.cbconnect.common.enums;

public enum TypeParametre {
    DELAI,
    TAUX,
    MONTANT,
    TEXTE,
    BOOLEEN,
    /**
     * Liste de valeurs paramétrables (professions, marques véhicules, etc.)
     * Convention clé : {CATEGORIE}.{CODE} ex: PROFESSION.MEDECIN
     */
    LISTE,
    /** Configuration mail (footer, URLs, identite emetteur) */
    MAIL,
    /** Parametres systeme (TTL, durees, flags techniques) */
    SYSTEM
}
