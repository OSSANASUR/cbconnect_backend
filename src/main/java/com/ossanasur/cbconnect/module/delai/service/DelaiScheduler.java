package com.ossanasur.cbconnect.module.delai.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@Slf4j @Component @RequiredArgsConstructor
public class DelaiScheduler {
    private final DelaiService delaiService;
    /** Recalcul des alertes chaque matin à 07h00 */
    @Scheduled(cron = "0 0 7 * * *")
    public void recalculQuotidien() {
        log.info("Démarrage recalcul quotidien des délais CEDEAO...");
        delaiService.recalculerAlertes();
    }
}
