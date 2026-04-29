package com.ossanasur.cbconnect.common.enums;

/**
 * Cycle de vie d'un registre journalier.
 *
 * OUVERT : la secrétaire saisit ses courriers du jour
 * CLOS   : la secrétaire a terminé la journée, le registre est figé et imprimé
 * VISE   : le chef a visé le registre (signature physique scannée)
 */
public enum StatutRegistre {
    OUVERT,
    CLOS,
    VISE
}
