package com.ossanasur.cbconnect.module.statistiques.controller;

import com.ossanasur.cbconnect.module.statistiques.dto.CadenceDto;
import com.ossanasur.cbconnect.module.statistiques.dto.EtatLitigeDto;
import com.ossanasur.cbconnect.module.statistiques.dto.EtatFinancierDto;
import com.ossanasur.cbconnect.module.statistiques.dto.EtatReclamationDto;
import com.ossanasur.cbconnect.module.statistiques.dto.ReclamationTogoDto;
import com.ossanasur.cbconnect.module.statistiques.dto.EtatSinistreDto;
import com.ossanasur.cbconnect.module.statistiques.dto.GraphiqueEncPaiDto;
import com.ossanasur.cbconnect.module.statistiques.dto.ReportingEncaissementDto;
import com.ossanasur.cbconnect.module.statistiques.dto.ReportingMensuelDto;
// import com.ossanasur.cbconnect.module.statistiques.dto.ReportingPaiementDto;
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
    // @GetMapping("/reporting-paiements")
    // @Operation(summary = "Reporting mensuel — tableaux comparatifs des sinistres
    // payés")
    // public ResponseEntity<DataResponse<ReportingPaiementDto>> reportingPaiements(
    // @RequestParam(defaultValue = "0") int annee,
    // @RequestParam(defaultValue = "0") int mois) {
    // if (annee == 0)
    // annee = LocalDate.now().getYear();
    // if (mois == 0)
    // mois = LocalDate.now().getMonthValue();
    // return ResponseEntity.ok(DataResponse.success("Reporting paiements",
    // statsService.reportingPaiements(annee, mois)));
    // }

    /**
     * Triangle de cadence — Sinistres par exercice de survenance.
     *
     * GET /v1/stats/cadence?annee=2024
     */
    @GetMapping("/cadence")
    @Operation(summary = "Triangle de cadence de règlement (survenus en × payés en)")
    public ResponseEntity<DataResponse<CadenceDto>> cadence(
            @RequestParam(defaultValue = "0") int annee) {
        if (annee == 0)
            annee = LocalDate.now().getYear();
        return ResponseEntity.ok(DataResponse.success("Cadence",
                statsService.cadence(annee)));
    }

    /**
     * Reporting mensuel paiements.
     * Réutilise ReportingEncaissementDto (structure identique).
     *
     * GET /v1/stats/reporting-paiements?annee=2025&mois=3
     */
    @GetMapping("/reporting-paiements")
    @Operation(summary = "Reporting mensuel — tableaux comparatifs des paiements")
    public ResponseEntity<DataResponse<ReportingEncaissementDto>> reportingPaiements(
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
     * Triangle de cadence des encaissements.
     *
     * GET /v1/stats/cadence-encaissements?annee=2024
     */
    @GetMapping("/cadence-encaissements")
    @Operation(summary = "Triangle de cadence des encaissements (survenus en × encaissés en)")
    public ResponseEntity<DataResponse<CadenceDto>> cadenceEncaissements(
            @RequestParam(defaultValue = "0") int annee) {
        if (annee == 0)
            annee = LocalDate.now().getYear();
        return ResponseEntity.ok(DataResponse.success("Cadence encaissements",
                statsService.cadenceEncaissements(annee)));
    }

    /**
     * État de réclamation global — double vue nb + montant.
     *
     * GET /v1/stats/reclamations
     */
    @GetMapping("/reclamations")
    @Operation(summary = "État de réclamation global — Togo ↔ Homologues")
    public ResponseEntity<DataResponse<EtatReclamationDto>> etatReclamation() {
        return ResponseEntity.ok(DataResponse.success("État réclamations",
                statsService.etatReclamation()));
    }

    /**
     * R6 — Détail des réclamations Togo envers les homologues (sinistres ET = SURVENU_TOGO).
     * Filtre optionnel par code pays émetteur (BF, CI, BJ, …).
     *
     * GET /v1/stats/reclamations-togo
     * GET /v1/stats/reclamations-togo?codePays=BF
     */
    @GetMapping("/reclamations-togo")
    @Operation(summary = "R6 — Réclamations Togo vs homologues (détail par compagnie)")
    public ResponseEntity<DataResponse<ReclamationTogoDto>> reclamationsTogo(
            @RequestParam(required = false) String codePays) {
        return ResponseEntity.ok(DataResponse.success("Réclamations Togo vs homologues",
                statsService.reclamationTogo(codePays)));
    }

    /**
     * État des litiges — Contentieux et Arbitrage en cours.
     * annee=0 → tous les exercices confondus.
     *
     * GET /v1/stats/litiges?annee=2026
     */
    @GetMapping("/litiges")
    @Operation(summary = "État des sinistres en litige (contentieux et arbitrage)")
    public ResponseEntity<DataResponse<EtatLitigeDto>> etatLitiges(
            @RequestParam(defaultValue = "0") int annee) {
        return ResponseEntity.ok(DataResponse.success("État litiges", statsService.etatLitiges(annee)));
    }

    /**
     * Graphique pluriannuel — Encaissements vs Paiements.
     * Par nombre de dossiers ET par montant, sur une plage d'années.
     *
     * GET /v1/stats/graphique-enc-pai?anneeDebut=2020&anneeFin=2026
     */
    @GetMapping("/graphique-enc-pai")
    @Operation(summary = "Graphique Encaissements vs Paiements — pluriannuel (nb + montant)")
    public ResponseEntity<DataResponse<GraphiqueEncPaiDto>> graphiqueEncPai(
            @RequestParam(defaultValue = "0") int anneeDebut,
            @RequestParam(defaultValue = "0") int anneeFin) {
        int fin = anneeFin == 0 ? LocalDate.now().getYear() : anneeFin;
        int debut = anneeDebut == 0 ? fin - 5 : anneeDebut;
        return ResponseEntity.ok(DataResponse.success("Graphique Enc/Pai",
                statsService.graphiqueEncPai(debut, fin)));
    }

}
