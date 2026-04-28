package com.ossanasur.cbconnect.module.finance.service.impl;

import com.ossanasur.cbconnect.common.enums.StatutPaiement;
import com.ossanasur.cbconnect.exception.BadRequestException;
import com.ossanasur.cbconnect.module.auth.repository.OrganismeRepository;
import com.ossanasur.cbconnect.module.auth.repository.UtilisateurRepository;
import com.ossanasur.cbconnect.module.comptabilite.repository.EcritureComptableRepository;
import com.ossanasur.cbconnect.module.comptabilite.service.ComptabiliteService;
import com.ossanasur.cbconnect.module.finance.dto.request.PaiementCreateRequest;
import com.ossanasur.cbconnect.module.finance.dto.request.ReglementComptableRequest;
import com.ossanasur.cbconnect.module.finance.entity.Paiement;
import com.ossanasur.cbconnect.module.finance.mapper.PaiementMapper;
import com.ossanasur.cbconnect.module.finance.repository.EncaissementRepository;
import com.ossanasur.cbconnect.module.finance.repository.PaiementRepository;
import com.ossanasur.cbconnect.module.finance.service.EncaissementGuardService;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import com.ossanasur.cbconnect.module.sinistre.repository.SinistreRepository;
import com.ossanasur.cbconnect.module.sinistre.repository.VictimeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaiementServiceImplGuardIntegrationTest {

    @Mock private PaiementRepository paiementRepository;
    @Mock private SinistreRepository sinistreRepository;
    @Mock private VictimeRepository victimeRepository;
    @Mock private OrganismeRepository organismeRepository;
    @Mock private EncaissementRepository encaissementRepository;
    @Mock private EcritureComptableRepository ecritureRepository;
    @Mock private UtilisateurRepository utilisateurRepository;
    @Mock private ComptabiliteService comptabiliteService;
    @Mock private PaiementMapper mapper;
    @Mock private EncaissementGuardService guardService;

    @InjectMocks private PaiementServiceImpl service;

    private final UUID SID = UUID.randomUUID();
    private final UUID PID = UUID.randomUUID();

    @Test
    void creer_appelleVerifierRegleA() {
        var sinistre = Sinistre.builder().sinistreTrackingId(SID).build();
        when(sinistreRepository.findActiveByTrackingId(SID)).thenReturn(Optional.of(sinistre));
        doThrow(new BadRequestException("aucun encaissement"))
                .when(guardService).verifierRegleA(SID);

        // PaiementCreateRequest: sinistreTrackingId, beneficiaire,
        // beneficiaireVictimeTrackingId, beneficiaireOrganismeTrackingId,
        // montant, typePrejudice, motifComplement
        var req = new PaiementCreateRequest(SID, "Bénéf",
                null, null,
                new BigDecimal("100000"),
                null, null);

        assertThatThrownBy(() -> service.creer(req, "user1"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("aucun encaissement");

        verify(guardService).verifierRegleA(SID);
        verifyNoInteractions(paiementRepository);
    }

    @Test
    void saisirReglementComptable_appelleVerifierRegleB_quandPasReprise() {
        var sinistre = Sinistre.builder().sinistreTrackingId(SID).build();
        var parent = Paiement.builder()
                .paiementTrackingId(PID).sinistre(sinistre)
                .statut(StatutPaiement.REGLEMENT_TECHNIQUE_VALIDE)
                .repriseHistorique(false)
                .build();
        when(paiementRepository.findActiveByTrackingId(PID)).thenReturn(Optional.of(parent));
        doThrow(new BadRequestException("crédité en banque"))
                .when(guardService).verifierRegleB(SID);

        var req = new ReglementComptableRequest("CHK001", "BANK", LocalDate.now(), LocalDate.now());

        assertThatThrownBy(() -> service.saisirReglementComptable(PID, req, "user1"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("crédité en banque");

        verify(guardService).verifierRegleB(SID);
    }

    @Test
    void saisirReglementComptable_bypassRepriseQuandFlagTrue() {
        var sinistre = Sinistre.builder().sinistreTrackingId(SID).build();
        var parent = Paiement.builder()
                .paiementTrackingId(PID).sinistre(sinistre)
                .statut(StatutPaiement.REGLEMENT_TECHNIQUE_VALIDE)
                .repriseHistorique(true)
                .build();
        when(paiementRepository.findActiveByTrackingId(PID)).thenReturn(Optional.of(parent));

        try {
            var req = new ReglementComptableRequest("CHK001", "BANK", LocalDate.now(), LocalDate.now());
            service.saisirReglementComptable(PID, req, "user1");
        } catch (Exception ignored) { /* irrelevant — only assertion is "guard.verifierRegleB never called" */ }

        verify(guardService, never()).verifierRegleB(any());
    }

    @Test
    void validerComptable_appelleVerifierRegleC_quandPasReprise() {
        var sinistre = Sinistre.builder().sinistreTrackingId(SID).build();
        var paiement = Paiement.builder()
                .paiementTrackingId(PID).sinistre(sinistre)
                .statut(StatutPaiement.REGLEMENT_COMPTABLE_VALIDE)
                .repriseHistorique(false)
                .build();
        when(paiementRepository.findActiveByTrackingId(PID)).thenReturn(Optional.of(paiement));
        doThrow(new BadRequestException("Couverture insuffisante"))
                .when(guardService).verifierRegleC(eq(SID), any());

        assertThatThrownBy(() -> service.validerComptable(PID, "user1"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Couverture insuffisante");

        verify(guardService).verifierRegleC(eq(SID), any(BigDecimal.class));
    }

    @Test
    void validerComptable_bypassRepriseQuandFlagTrue() {
        var sinistre = Sinistre.builder().sinistreTrackingId(SID).build();
        var paiement = Paiement.builder()
                .paiementTrackingId(PID).sinistre(sinistre)
                .statut(StatutPaiement.REGLEMENT_COMPTABLE_VALIDE)
                .repriseHistorique(true)
                .build();
        when(paiementRepository.findActiveByTrackingId(PID)).thenReturn(Optional.of(paiement));
        when(paiementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        try { service.validerComptable(PID, "user1"); } catch (Exception ignored) {}

        verify(guardService, never()).verifierRegleC(any(), any());
    }
}
