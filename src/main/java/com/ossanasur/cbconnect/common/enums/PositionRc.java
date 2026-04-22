package com.ossanasur.cbconnect.common.enums;

/**
 * États de la position RC dans la négociation BNCB Togo ↔ compagnie étrangère.
 *
 *   EN_ATTENTE      → dossier créé, aucune proposition BNCB faite
 *   EN_NEGOCIATION  → proposition envoyée, en attente réponse compagnie
 *   REJETEE         → compagnie a rejeté, contre-proposition BNCB attendue
 *   TRANCHEE        → accord final, pourcentage verrouillé définitivement
 */
public enum PositionRc { EN_ATTENTE, EN_NEGOCIATION, REJETEE, TRANCHEE }
