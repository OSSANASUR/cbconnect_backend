package com.ossanasur.cbconnect.module.reprise.controller;

import com.ossanasur.cbconnect.module.reprise.dto.RapportReprise;
import com.ossanasur.cbconnect.module.reprise.dto.RepriseEncaissementsRequest;
import com.ossanasur.cbconnect.module.reprise.dto.RepriseOrganismesRequest;
import com.ossanasur.cbconnect.module.reprise.dto.ReprisePaiementsRequest;
import com.ossanasur.cbconnect.module.reprise.dto.RepriseSinistresRequest;
import com.ossanasur.cbconnect.module.reprise.service.RepriseService;
import com.ossanasur.cbconnect.utils.DataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints de reprise des données historiques CBConnect.
 *
 * Flux d'utilisation :
 * 1. Le frontend parse le fichier Excel directement (client-side, SheetJS)
 * 2. Envoie les données en JSON par batch de 50 → POST /v1/reprise/sinistres
 * 3. Reçoit le rapport d'import (importés / doublons / erreurs)
 * 4. POST /v1/reprise/organismes pour les compagnies CEDEAO
 */
@Slf4j
@RestController
@RequestMapping("/v1/reprise")
@RequiredArgsConstructor
@Tag(name = "Reprise historique", description = "Import des données historiques 2025")
public class RepriseController {

        private final RepriseService repriseService;

        /**
         * Import batch de sinistres historiques.
         * Reçoit jusqu'à 50 sinistres par appel (batching côté frontend).
         * Idempotent : les doublons (même numeroSinistreManuel) sont ignorés.
         */
        @PostMapping("/sinistres")
        @Operation(summary = "Import batch sinistres historiques", description = "Crée les sinistres ET et TE depuis le fichier Excel 2025. "
                        +
                        "Idempotent — les doublons sont détectés par numeroSinistreManuel.")
        public ResponseEntity<DataResponse<RapportReprise>> importerSinistres(
                        @Valid @RequestBody RepriseSinistresRequest request,
                        Authentication auth) {

                log.info("[REPRISE] Import batch {} sinistres par {}",
                                request.sinistres().size(),
                                auth != null ? auth.getName() : "system");

                String loginAuteur = auth != null ? auth.getName() : "reprise";
                RapportReprise rapport = repriseService.importerSinistres(request, loginAuteur);

                log.info("[REPRISE] Batch terminé : {} importés, {} doublons, {} erreurs",
                                rapport.importes(), rapport.doublons(), rapport.erreurs());

                return ResponseEntity.ok(DataResponse.success("Batch importé", rapport));
        }

        /**
         * Import des compagnies d'assurance CEDEAO.
         * Crée les organismes manquants, ignore les doublons (même code).
         */
        @PostMapping("/organismes")
        @Operation(summary = "Import compagnies CEDEAO", description = "Crée tous les organismes membres CEDEAO (source : CODE_COMPAGNIES_ASSURANCE_CEDEAO). "
                        +
                        "Idempotent — doublons détectés par code.")
        public ResponseEntity<DataResponse<RapportReprise>> importerOrganismes(
                        @Valid @RequestBody RepriseOrganismesRequest request,
                        Authentication auth) {

                log.info("[REPRISE] Import {} organismes CEDEAO", request.organismes().size());

                String loginAuteur = auth != null ? auth.getName() : "reprise";
                RapportReprise rapport = repriseService.importerOrganismes(request, loginAuteur);

                return ResponseEntity.ok(DataResponse.success("Organismes importés", rapport));
        }

        /**
         * Vérification rapide : compte les sinistres déjà importés par reprise.
         */
        @GetMapping("/status")
        @Operation(summary = "Statut de la reprise")
        public ResponseEntity<DataResponse<java.util.Map<String, Object>>> statutReprise() {
                return ResponseEntity.ok(DataResponse.success("Statut reprise", repriseService.getStatut()));
        }

        /**
         * Import batch d'encaissements historiques.
         * Crée les sinistres minimaux manquants au besoin.
         *
         * POST /v1/reprise/encaissements
         */
        @PostMapping("/encaissements")
        @Operation(summary = "Import batch encaissements historiques", description = "Importe les encaissements depuis le fichier Excel BNCB. "
                        +
                        "Doublon détecté sur numeroCheque + organismeEmetteur. " +
                        "Sinistre absent → création minimale automatique.")
        public ResponseEntity<DataResponse<RapportReprise>> importerEncaissements(
                        @Valid @RequestBody RepriseEncaissementsRequest request,
                        Authentication auth) {

                log.info("[REPRISE] Import batch {} encaissements par {}",
                                request.encaissements().size(),
                                auth != null ? auth.getName() : "system");

                String loginAuteur = auth != null ? auth.getName() : "reprise";
                RapportReprise rapport = repriseService.importerEncaissements(request, loginAuteur);

                log.info("[REPRISE] Encaissements — {} importés, {} doublons, {} erreurs",
                                rapport.importes(), rapport.doublons(), rapport.erreurs());

                return ResponseEntity.ok(DataResponse.success("Encaissements importés", rapport));
        }

        /**
         * Import batch de paiements historiques.
         * Crée les sinistres et victimes minimaux manquants au besoin.
         * Lie automatiquement chaque paiement à son(ses) encaissement(s) via
         * la table paiement_encaissement.
         *
         * POST /v1/reprise/paiements
         */
        @PostMapping("/paiements")
        @Operation(summary = "Import batch paiements historiques", description = "Importe les paiements depuis le fichier Excel BNCB. "
                        + "Doublon détecté sur numeroChequeEmis. "
                        + "Sinistre et victime absents → création minimale automatique. "
                        + "Liaison paiement ↔ encaissement via table de jointure.")
        public ResponseEntity<DataResponse<RapportReprise>> importerPaiements(
                        @Valid @RequestBody ReprisePaiementsRequest request,
                        Authentication auth) {

                log.info("[REPRISE] Import batch {} paiements par {}",
                                request.paiements().size(),
                                auth != null ? auth.getName() : "system");

                String loginAuteur = auth != null ? auth.getName() : "reprise";
                RapportReprise rapport = repriseService.importerPaiements(request, loginAuteur);

                log.info("[REPRISE] Paiements — {} importés, {} doublons, {} erreurs",
                                rapport.importes(), rapport.doublons(), rapport.erreurs());

                return ResponseEntity.ok(DataResponse.success("Paiements importés", rapport));
        }

}
