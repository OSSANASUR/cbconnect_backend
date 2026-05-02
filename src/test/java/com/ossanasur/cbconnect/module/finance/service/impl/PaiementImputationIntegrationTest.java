package com.ossanasur.cbconnect.module.finance.service.impl;

import com.ossanasur.cbconnect.common.enums.CategorieReglement;
import com.ossanasur.cbconnect.common.enums.TypeOrganisme;
import com.ossanasur.cbconnect.common.enums.TypePrejudice;
import com.ossanasur.cbconnect.module.auth.entity.Organisme;
import com.ossanasur.cbconnect.module.auth.repository.OrganismeRepository;
import com.ossanasur.cbconnect.module.finance.dto.request.AnnulerPaiementRequest;
import com.ossanasur.cbconnect.module.finance.dto.request.ImputationRequest;
import com.ossanasur.cbconnect.module.finance.dto.request.PaiementCreateRequest;
import com.ossanasur.cbconnect.module.finance.dto.response.PaiementDetailResponse;
import com.ossanasur.cbconnect.module.finance.entity.PaiementImputation;
import com.ossanasur.cbconnect.module.finance.repository.PaiementImputationRepository;
import com.ossanasur.cbconnect.module.finance.repository.PaiementRepository;
import com.ossanasur.cbconnect.module.finance.service.PaiementImputationService;
import com.ossanasur.cbconnect.module.finance.service.PaiementService;
import com.ossanasur.cbconnect.utils.DataResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test d'intégration bout-en-bout pour le workflow d'imputation (Phase D4).
 *
 * <p>Valide le câblage Spring/JPA complet :
 * <ol>
 *   <li>Création d'un RT 75k imputé sur 2 encaissements (50k + 25k).</li>
 *   <li>Vérification des 2 lignes positives et des restes disponibles.</li>
 *   <li>Annulation du RT : contre-passages automatiques.</li>
 *   <li>Vérification que les restes sont intégralement libérés.</li>
 * </ol>
 *
 * <p>Nécessite une vraie base PostgreSQL de test (feedback utilisateur — pas de
 * mock DB). Les fixtures sont insérées via {@code @Sql} et annulées par
 * {@code @Rollback} après chaque test.
 *
 * <p>Approche retenue : {@code @Sql("/test-imputation-fixtures.sql")} pour les
 * entités avec de nombreuses FK obligatoires (Sinistre → Pays + Assure ;
 * Encaissement → Organisme), puis appels via les vraies interfaces de service.
 */
@SpringBootTest
@Transactional
@Rollback
class PaiementImputationIntegrationTest {

    // ── UUIDs constants définis dans test-imputation-fixtures.sql ─────────────
    private static final UUID SIN_TID = UUID.fromString("s0000000-0000-0000-0000-000000000001");
    private static final UUID ENC1_TID = UUID.fromString("e1000000-0000-0000-0000-000000000001");
    private static final UUID ENC2_TID = UUID.fromString("e2000000-0000-0000-0000-000000000002");

    @Autowired private PaiementService paiementService;
    @Autowired private PaiementImputationService paiementImputationService;
    @Autowired private PaiementRepository paiementRepository;
    @Autowired private PaiementImputationRepository imputationRepository;
    @Autowired private OrganismeRepository organismeRepository;

    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @Sql("/test-imputation-fixtures.sql")
    void workflowComplet_creationRT_puisAnnulation_imputationsCorrectes() {

        // ─── Arrange : récupérer l'organisme BNCB comme bénéficiaire ─────────
        // Le bénéficiaire Organisme évite les contraintes de la règle PROVISIONS/VICTIME.
        // PaiementBeneficiaireValidator autorise un Organisme pour PRINCIPAL.
        Organisme bncb = organismeRepository
                .findAllActiveByType(TypeOrganisme.BUREAU_NATIONAL)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Aucun organisme BUREAU_NATIONAL en base — vérifier les migrations Flyway"));

        // ─── Act 1 : créer RT 75k imputé 50k sur enc1 + 25k sur enc2 ─────────
        PaiementCreateRequest req = new PaiementCreateRequest(
                SIN_TID,
                "Organisme Test D4",
                /* beneficiaireVictimeTrackingId  */ null,
                /* beneficiaireOrganismeTrackingId */ bncb.getOrganismeTrackingId(),
                new BigDecimal("75000"),
                TypePrejudice.MATERIEL,
                /* motifComplement */ null,
                /* beneficiaireExpertTrackingId */ null,
                CategorieReglement.PRINCIPAL,
                "Règlement test D4",
                List.of(
                        new ImputationRequest(ENC1_TID, new BigDecimal("50000")),
                        new ImputationRequest(ENC2_TID, new BigDecimal("25000"))
                )
        );

        DataResponse<PaiementDetailResponse> resp = paiementService.creer(req, "test-user-d4");
        assertThat(resp).isNotNull();
        assertThat(resp.getData()).isNotNull();

        UUID rtTrackingId = resp.getData().paiementTrackingId();
        assertThat(rtTrackingId).isNotNull();

        // Recharger l'entité persistée pour obtenir son historiqueId
        var rt = paiementRepository.findActiveByTrackingId(rtTrackingId)
                .orElseThrow(() -> new AssertionError("RT introuvable après creer() : " + rtTrackingId));

        // ─── Assert 1 : 2 imputations positives persistées ───────────────────
        List<PaiementImputation> imps = imputationRepository.findActiveByPaiement(rt.getHistoriqueId());
        assertThat(imps)
                .as("2 imputations positives attendues pour le RT")
                .hasSize(2);
        assertThat(imps)
                .as("Toutes les imputations initiales doivent être positives")
                .allMatch(pi -> pi.getMontantImpute().signum() > 0);

        // ─── Assert 2 : restes disponibles après imputation ──────────────────
        assertThat(paiementImputationService.getResteDisponible(ENC1_TID))
                .as("Reste enc1 doit être 0 (50k imputés sur 50k)")
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(paiementImputationService.getResteDisponible(ENC2_TID))
                .as("Reste enc2 doit être 25k (25k imputés sur 50k)")
                .isEqualByComparingTo(new BigDecimal("25000"));

        // ─── Act 2 : annuler le RT ────────────────────────────────────────────
        paiementService.annuler(
                rtTrackingId,
                new AnnulerPaiementRequest("Erreur de saisie", "Erreur de saisie — test D4"),
                "test-user-d4");

        // ─── Assert 3 : contre-passages générés — restes intégralement libérés
        assertThat(paiementImputationService.getResteDisponible(ENC1_TID))
                .as("Reste enc1 doit être entièrement libéré après annulation (50k)")
                .isEqualByComparingTo(new BigDecimal("50000"));
        assertThat(paiementImputationService.getResteDisponible(ENC2_TID))
                .as("Reste enc2 doit être entièrement libéré après annulation (50k)")
                .isEqualByComparingTo(new BigDecimal("50000"));
    }
}
