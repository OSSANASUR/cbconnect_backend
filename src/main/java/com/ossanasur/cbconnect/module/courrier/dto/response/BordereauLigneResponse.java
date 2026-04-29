package com.ossanasur.cbconnect.module.courrier.dto.response;

import com.ossanasur.cbconnect.common.enums.NatureCourrier;

import java.util.UUID;

/**
 * Une ligne imprimée sur le bordereau de transmission — mirror des colonnes
 * du document physique BNCB.
 */
public record BordereauLigneResponse(
    UUID courrierTrackingId,
    Integer ordreDansBordereau,
    /** N° SIN OUR REF */
    String numeroSinistreOurRef,
    /** OBJET */
    NatureCourrier objet,
    /** N° SIN VOTRE REF — n° sinistre côté homologue */
    String numeroSinistreVotreRef,
    /** REFERENCE DU COURRIER */
    String referenceCourrier,
    /** Objet détaillé (texte libre, pour infobulle) */
    String objetDetaille
) {}
