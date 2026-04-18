package com.ossanasur.cbconnect.common.enums;

public enum StatutPiece {
    /** Pièce attendue — pas encore reçue ni associée dans la GED */
    ATTENDUE,
    /** Document GED reçu et associé à ce type de pièce */
    RECUE,
    /** Document reçu mais refusé (illisible, mauvais document, etc.) */
    REJETEE
}
