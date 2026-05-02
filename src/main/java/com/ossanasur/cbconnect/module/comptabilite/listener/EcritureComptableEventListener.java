package com.ossanasur.cbconnect.module.comptabilite.listener;

import com.ossanasur.cbconnect.module.comptabilite.event.EcritureComptableEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listener placeholder pour EcritureComptableEvent.
 * À implémenter dans le plan dédié "Plan comptable + génération auto".
 * Pour l'instant : log les événements pour traçabilité dev.
 */
@Component
@Slf4j
public class EcritureComptableEventListener {

    @EventListener
    public void handle(EcritureComptableEvent event) {
        log.info("EcritureComptableEvent reçu : type={}, paiement={}, encaissement={}, montant={}, libellé={}, auteur={}",
                event.getType(),
                event.getPaiementTrackingId(),
                event.getEncaissementTrackingId(),
                event.getMontant(),
                event.getLibelle(),
                event.getAuteur());
        // TODO(comptabilite): générer EcritureComptable selon plan comptable.
    }
}
