package com.ossanasur.cbconnect.module.finance.service.impl;

import com.ossanasur.cbconnect.module.finance.dto.response.CouvertureFinanciereResponse;
import com.ossanasur.cbconnect.module.finance.mapper.PrefinancementMapper;
import com.ossanasur.cbconnect.module.finance.repository.EncaissementRepository;
import com.ossanasur.cbconnect.module.finance.repository.PaiementRepository;
import com.ossanasur.cbconnect.module.finance.repository.PrefinancementRemboursementRepository;
import com.ossanasur.cbconnect.module.finance.repository.PrefinancementRepository;
import com.ossanasur.cbconnect.module.finance.service.NumeroOperationGenerator;
import com.ossanasur.cbconnect.module.auth.repository.UtilisateurRepository;
import com.ossanasur.cbconnect.module.sinistre.repository.SinistreRepository;
import com.ossanasur.cbconnect.utils.DataResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Tests régression sur le bug #2 : getCouvertureFinanciere ne doit pas
 * double-compter encaissements + préfinancements.
 */
@ExtendWith(MockitoExtension.class)
class PrefinancementServiceImplCouvertureTest {

    @Mock private PrefinancementRepository prefinancementRepository;
    @Mock private PrefinancementRemboursementRepository remboursementRepository;
    @Mock private SinistreRepository sinistreRepository;
    @Mock private EncaissementRepository encaissementRepository;
    @Mock private UtilisateurRepository utilisateurRepository;
    @Mock private NumeroOperationGenerator numeroOperationGenerator;
    @Mock private PrefinancementMapper mapper;
    @Mock private PaiementRepository paiementRepository;

    @InjectMocks private PrefinancementServiceImpl service;

    @Test
    void getCouvertureFinanciere_avecPrefiEtEnc_pasDeDoubleComptage() {
        UUID sid = UUID.randomUUID();

        when(encaissementRepository.sumMontantActifBySinistre(sid))
                .thenReturn(new BigDecimal("100000"));
        when(prefinancementRepository.sumMontantActifBySinistre(sid))
                .thenReturn(new BigDecimal("100000"));
        when(paiementRepository.sumMontantActifBySinistre(sid))
                .thenReturn(BigDecimal.ZERO);

        // Stubs pour Règles A / B
        lenient().when(encaissementRepository.existsActifNonAnnuleBySinistre(sid)).thenReturn(true);
        lenient().when(prefinancementRepository.existsActifBySinistre(sid)).thenReturn(true);
        lenient().when(encaissementRepository.existsEncaisseBySinistre(sid)).thenReturn(true);

        DataResponse<CouvertureFinanciereResponse> result = service.getCouvertureFinanciere(sid);

        // Solde net = 100k - 100k - 0 = 0 (et NON 200k comme dans l'ancien bug).
        assertThat(result.getData().soldeNet()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getCouvertureFinanciere_sansPrefi_calculSimple() {
        UUID sid = UUID.randomUUID();

        when(encaissementRepository.sumMontantActifBySinistre(sid))
                .thenReturn(new BigDecimal("150000"));
        when(prefinancementRepository.sumMontantActifBySinistre(sid))
                .thenReturn(BigDecimal.ZERO);
        when(paiementRepository.sumMontantActifBySinistre(sid))
                .thenReturn(new BigDecimal("50000"));

        lenient().when(encaissementRepository.existsActifNonAnnuleBySinistre(sid)).thenReturn(true);
        lenient().when(prefinancementRepository.existsActifBySinistre(sid)).thenReturn(false);
        lenient().when(encaissementRepository.existsEncaisseBySinistre(sid)).thenReturn(true);

        DataResponse<CouvertureFinanciereResponse> result = service.getCouvertureFinanciere(sid);

        // Solde = 150k - 0 - 50k = 100k.
        assertThat(result.getData().soldeNet()).isEqualByComparingTo(new BigDecimal("100000"));
    }
}
