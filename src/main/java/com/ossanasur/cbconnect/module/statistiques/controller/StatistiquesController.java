package com.ossanasur.cbconnect.module.statistiques.controller;

import com.ossanasur.cbconnect.module.statistiques.dto.EtatFinancierDto;
import com.ossanasur.cbconnect.module.statistiques.dto.EtatSinistreDto;
import com.ossanasur.cbconnect.module.statistiques.service.StatistiquesService;
import com.ossanasur.cbconnect.utils.DataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/v1/stats")
@RequiredArgsConstructor
@Tag(name = "Statistiques", description = "États statistiques BNCB")
public class StatistiquesController {

    private final StatistiquesService statsService;

    /**
     * État I — Sinistres déclarés par pays émetteur.
     * Comparaison N-1 vs N avec variation et répartition en %.
     *
     * GET /v1/stats/sinistres?annee=2025
     */
    @GetMapping("/sinistres")
    @Operation(summary = "État des sinistres déclarés par pays")
    public ResponseEntity<DataResponse<EtatSinistreDto>> etatSinistres(
            @RequestParam(defaultValue = "0") int annee) {
        if (annee == 0)
            annee = LocalDate.now().getYear();
        return ResponseEntity.ok(DataResponse.success("État sinistres", statsService.etatSinistres(annee)));
    }

    /**
     * État II — Encaissements et paiements.
     * Par pays émetteur + détail compagnies Togo.
     *
     * GET /v1/stats/financier?annee=2025
     */
    @GetMapping("/financier")
    @Operation(summary = "État des encaissements et paiements")
    public ResponseEntity<DataResponse<EtatFinancierDto>> etatFinancier(
            @RequestParam(defaultValue = "0") int annee) {
        if (annee == 0)
            annee = LocalDate.now().getYear();
        return ResponseEntity.ok(DataResponse.success("État financier", statsService.etatFinancier(annee)));
    }
}
