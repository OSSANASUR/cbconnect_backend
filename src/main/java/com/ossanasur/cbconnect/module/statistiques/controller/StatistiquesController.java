package com.ossanasur.cbconnect.module.statistiques.controller;

import com.ossanasur.cbconnect.module.statistiques.dto.CadenceDto;
import com.ossanasur.cbconnect.module.statistiques.dto.EtatFinancierDto;
import com.ossanasur.cbconnect.module.statistiques.dto.EtatSinistreDto;
import com.ossanasur.cbconnect.module.statistiques.dto.ReportingEncaissementDto;
import com.ossanasur.cbconnect.module.statistiques.dto.ReportingMensuelDto;
import com.ossanasur.cbconnect.module.statistiques.dto.ReportingPaiementDto;
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

    /**
     * Reporting mensuel BNCB-TG — Tableaux comparatifs des sinistres enregistrés.
     * Tableau 1 : par pays partenaire CEDEAO.
     * Tableau 2 : par compagnie membre togolaise (CIMA RC Auto).
     *
     * GET /v1/stats/reporting-mensuel?annee=2025&mois=3
     */
    @GetMapping("/reporting-mensuel")
    @Operation(summary = "Reporting mensuel — tableaux comparatifs des sinistres enregistrés")
    public ResponseEntity<DataResponse<ReportingMensuelDto>> reportingMensuel(
            @RequestParam(defaultValue = "0") int annee,
            @RequestParam(defaultValue = "0") int mois) {
        if (annee == 0)
            annee = LocalDate.now().getYear();
        if (mois == 0)
            mois = LocalDate.now().getMonthValue();
        return ResponseEntity.ok(DataResponse.success("Reporting mensuel",
                statsService.reportingMensuel(annee, mois)));
    }

    /**
     * Reporting mensuel encaissements — Tableaux comparatifs des sinistres
     * encaissés.
     * Tableau I : par pays payeur (inclut TOGO).
     * Tableau II : par compagnie membre togolaise (marché togolais).
     *
     * GET /v1/stats/reporting-encaissements?annee=2025&mois=3
     */
    @GetMapping("/reporting-encaissements")
    @Operation(summary = "Reporting mensuel — tableaux comparatifs des encaissements")
    public ResponseEntity<DataResponse<ReportingEncaissementDto>> reportingEncaissements(
            @RequestParam(defaultValue = "0") int annee,
            @RequestParam(defaultValue = "0") int mois) {
        if (annee == 0)
            annee = LocalDate.now().getYear();
        if (mois == 0)
            mois = LocalDate.now().getMonthValue();
        return ResponseEntity.ok(DataResponse.success("Reporting encaissements",
                statsService.reportingEncaissements(annee, mois)));
    }

    /**
     * Reporting mensuel paiements — Tableaux comparatifs des sinistres payés.
     * Tableau I : par pays bénéficiaire (pays émetteur du sinistre).
     * Tableau II : par compagnie membre togolaise (marché togolais).
     *
     * GET /v1/stats/reporting-paiements?annee=2025&mois=3
     */
    @GetMapping("/reporting-paiements")
    @Operation(summary = "Reporting mensuel — tableaux comparatifs des sinistres payés")
    public ResponseEntity<DataResponse<ReportingPaiementDto>> reportingPaiements(
            @RequestParam(defaultValue = "0") int annee,
            @RequestParam(defaultValue = "0") int mois) {
        if (annee == 0)
            annee = LocalDate.now().getYear();
        if (mois == 0)
            mois = LocalDate.now().getMonthValue();
        return ResponseEntity.ok(DataResponse.success("Reporting paiements",
                statsService.reportingPaiements(annee, mois)));
    }

    /**
     * R4 — Cadence de survenance par rapport au paiement.
     * Tableau TOTAL + par pays + par compagnie togolaise.
     *
     * GET /v1/stats/cadence?annee=2026&mois=1
     */
    @GetMapping("/cadence")
    @Operation(summary = "Cadence de survenance par rapport au paiement (R4)")
    public ResponseEntity<DataResponse<CadenceDto>> cadenceSurvenance(
            @RequestParam(defaultValue = "0") int annee,
            @RequestParam(defaultValue = "0") int mois) {
        if (annee == 0)
            annee = LocalDate.now().getYear();
        if (mois == 0)
            mois = LocalDate.now().getMonthValue();
        return ResponseEntity.ok(DataResponse.success("Cadence survenance",
                statsService.cadenceSurvenance(annee, mois)));
    }
}
