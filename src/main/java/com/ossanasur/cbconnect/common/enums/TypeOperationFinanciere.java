package com.ossanasur.cbconnect.common.enums;

/**
 * Type d'opération financière, dérivé du contexte d'une ligne Paiement/Prefinancement.
 * Utilisé comme préfixe du numéro d'opération (RT/RC/PF/AN).
 */
public enum TypeOperationFinanciere {
    REGLEMENT_TECHNIQUE("RT"),
    REGLEMENT_COMPTABLE("RC"),
    PREFINANCEMENT("PF"),
    ANNULATION_REGLEMENT("AN");

    private final String code;

    TypeOperationFinanciere(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
