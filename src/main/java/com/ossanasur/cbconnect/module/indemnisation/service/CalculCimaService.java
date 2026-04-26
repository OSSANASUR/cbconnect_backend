package com.ossanasur.cbconnect.module.indemnisation.service;

import com.ossanasur.cbconnect.module.indemnisation.dto.request.CalculRequest;
import com.ossanasur.cbconnect.module.indemnisation.entity.OffreIndemnisation;
import com.ossanasur.cbconnect.module.sinistre.entity.Victime;

/**
 * Interface du service de calcul CIMA (art. 258-266).
 * Implémenté par CalculCimaServiceImpl qui concentre toutes les formules
 * légales.
 */
public interface CalculCimaService {
    /** Calcule et sauvegarde une offre d'indemnisation pour une victime blessée */
    OffreIndemnisation calculerOffreBlesse(Victime victime, CalculRequest params, String loginAuteur);

    /** Calcule et sauvegarde une offre d'indemnisation pour une victime décédée */
    OffreIndemnisation calculerOffreDeces(Victime victime, CalculRequest params, String loginAuteur);

    /** Recalcule les pénalités de retard pour une offre dépassant les délais */
    java.math.BigDecimal calculerPenalitesRetard(OffreIndemnisation offre);
}