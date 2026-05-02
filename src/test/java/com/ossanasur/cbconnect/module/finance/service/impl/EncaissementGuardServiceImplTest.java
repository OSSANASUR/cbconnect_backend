package com.ossanasur.cbconnect.module.finance.service.impl;

import com.ossanasur.cbconnect.exception.BadRequestException;
import com.ossanasur.cbconnect.module.finance.repository.EncaissementRepository;
import com.ossanasur.cbconnect.module.finance.repository.PaiementRepository;
import com.ossanasur.cbconnect.module.finance.repository.PrefinancementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EncaissementGuardServiceImplTest {

    @Mock private EncaissementRepository encaissementRepository;
    @Mock private PaiementRepository paiementRepository;
    @Mock private PrefinancementRepository prefinancementRepository;
    @InjectMocks private EncaissementGuardServiceImpl guard;

    private final UUID SID = UUID.randomUUID();

    // ---- Règle A ------------------------------------------------------------

    @Test
    void regleA_passe_quandUnEncaissementNonAnnuleExiste() {
        when(encaissementRepository.existsNonAnnuleBySinistre(SID)).thenReturn(true);

        assertThatCode(() -> guard.verifierRegleA(SID)).doesNotThrowAnyException();
    }

    @Test
    void regleA_echoue_quandAucunEncaissement() {
        when(encaissementRepository.existsNonAnnuleBySinistre(SID)).thenReturn(false);

        assertThatThrownBy(() -> guard.verifierRegleA(SID))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("aucun encaissement");
    }

    // ---- Règle B ------------------------------------------------------------

    @Test
    void regleB_passe_quandSommeEncaisseStrictementPositive() {
        when(encaissementRepository.sumMontantEncaisseBySinistre(SID))
                .thenReturn(new BigDecimal("100000.00"));

        assertThatCode(() -> guard.verifierRegleB(SID)).doesNotThrowAnyException();
    }

    @Test
    void regleB_echoue_quandSommeEncaisseEstZero() {
        when(encaissementRepository.sumMontantEncaisseBySinistre(SID))
                .thenReturn(BigDecimal.ZERO);

        assertThatThrownBy(() -> guard.verifierRegleB(SID))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("crédité en banque");
    }

    @Test
    void regleB_echoue_quandSommeEncaisseEstNull() {
        when(encaissementRepository.sumMontantEncaisseBySinistre(SID)).thenReturn(null);

        assertThatThrownBy(() -> guard.verifierRegleB(SID))
                .isInstanceOf(BadRequestException.class);
    }

    // ---- Règle C ------------------------------------------------------------

    @Test
    void regleC_passe_quandCouvertureSuffisante() {
        when(encaissementRepository.sumMontantEncaisseBySinistre(SID))
                .thenReturn(new BigDecimal("500000"));
        when(paiementRepository.sumMontantActifBySinistre(SID))
                .thenReturn(new BigDecimal("300000"));

        assertThatCode(() -> guard.verifierRegleC(SID, new BigDecimal("100000")))
                .doesNotThrowAnyException();
    }

    @Test
    void regleC_echoue_quandCouvertureInsuffisante() {
        when(encaissementRepository.sumMontantEncaisseBySinistre(SID))
                .thenReturn(new BigDecimal("100000"));
        when(paiementRepository.sumMontantActifBySinistre(SID))
                .thenReturn(new BigDecimal("80000"));

        assertThatThrownBy(() -> guard.verifierRegleC(SID, new BigDecimal("50000")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Couverture financière insuffisante")
                .hasMessageContaining("30000");
    }

    @Test
    void regleC_montantNouveauZero_validerComptable() {
        when(encaissementRepository.sumMontantEncaisseBySinistre(SID))
                .thenReturn(new BigDecimal("100000"));
        when(paiementRepository.sumMontantActifBySinistre(SID))
                .thenReturn(new BigDecimal("100000"));

        assertThatCode(() -> guard.verifierRegleC(SID, BigDecimal.ZERO))
                .doesNotThrowAnyException();
    }

    @Test
    void regleC_geresLesNulls() {
        when(encaissementRepository.sumMontantEncaisseBySinistre(SID)).thenReturn(null);
        when(paiementRepository.sumMontantActifBySinistre(SID)).thenReturn(null);

        assertThatThrownBy(() -> guard.verifierRegleC(SID, new BigDecimal("100000")))
                .isInstanceOf(BadRequestException.class);
    }

    // ─────── Tests régression bug #1 (cf. plan imputation-encaissement, Task C8) ───────

    @Test
    void verifierRegleC_sinistreAvecPrefiNonRembourse_throwsBadRequest() {
        UUID sid = UUID.randomUUID();
        when(encaissementRepository.sumMontantActifBySinistre(sid))
                .thenReturn(new BigDecimal("100000"));
        when(prefinancementRepository.sumMontantActifBySinistre(sid))
                .thenReturn(new BigDecimal("100000"));
        when(paiementRepository.sumMontantActifBySinistre(sid))
                .thenReturn(BigDecimal.ZERO);

        // Solde réel = 100k - 100k = 0. Demander 1 FCFA doit être bloqué.
        assertThatThrownBy(() -> guard.verifierRegleC(sid, BigDecimal.ONE))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Couverture financière insuffisante");
    }

    @Test
    void verifierRegleC_sinistreSansPrefi_passeNormalement() {
        UUID sid = UUID.randomUUID();
        when(encaissementRepository.sumMontantActifBySinistre(sid))
                .thenReturn(new BigDecimal("100000"));
        when(prefinancementRepository.sumMontantActifBySinistre(sid))
                .thenReturn(BigDecimal.ZERO);
        when(paiementRepository.sumMontantActifBySinistre(sid))
                .thenReturn(new BigDecimal("25000"));

        // Solde = 100k - 0 - 25k = 75k. Demander 50k doit passer.
        assertThatCode(() -> guard.verifierRegleC(sid, new BigDecimal("50000")))
                .doesNotThrowAnyException();
    }

    @Test
    void verifierRegleC_avecPrefiPartiel_calculeBienLeReste() {
        UUID sid = UUID.randomUUID();
        when(encaissementRepository.sumMontantActifBySinistre(sid))
                .thenReturn(new BigDecimal("200000"));
        when(prefinancementRepository.sumMontantActifBySinistre(sid))
                .thenReturn(new BigDecimal("60000"));
        when(paiementRepository.sumMontantActifBySinistre(sid))
                .thenReturn(new BigDecimal("80000"));

        // Solde = 200k - 60k - 80k = 60k. Demander 60k doit juste passer.
        assertThatCode(() -> guard.verifierRegleC(sid, new BigDecimal("60000")))
                .doesNotThrowAnyException();

        // Demander 60k + 1 doit échouer.
        assertThatThrownBy(() -> guard.verifierRegleC(sid, new BigDecimal("60001")))
                .isInstanceOf(BadRequestException.class);
    }
}
