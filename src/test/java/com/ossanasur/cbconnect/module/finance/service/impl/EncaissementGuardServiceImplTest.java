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
}
