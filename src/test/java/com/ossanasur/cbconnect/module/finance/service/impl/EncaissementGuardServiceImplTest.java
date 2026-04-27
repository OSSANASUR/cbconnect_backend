package com.ossanasur.cbconnect.module.finance.service.impl;

import com.ossanasur.cbconnect.exception.BadRequestException;
import com.ossanasur.cbconnect.module.finance.repository.EncaissementRepository;
import com.ossanasur.cbconnect.module.finance.repository.PaiementRepository;
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
}
