package com.ossanasur.cbconnect.module.finance.service.impl;

import com.ossanasur.cbconnect.common.enums.StatutPaiement;
import com.ossanasur.cbconnect.exception.BadRequestException;
import com.ossanasur.cbconnect.module.finance.dto.request.ImputationRequest;
import com.ossanasur.cbconnect.module.finance.entity.Encaissement;
import com.ossanasur.cbconnect.module.finance.entity.Paiement;
import com.ossanasur.cbconnect.module.finance.entity.PaiementImputation;
import com.ossanasur.cbconnect.module.finance.mapper.PaiementImputationMapper;
import com.ossanasur.cbconnect.module.finance.repository.EncaissementRepository;
import com.ossanasur.cbconnect.module.finance.repository.PaiementImputationRepository;
import com.ossanasur.cbconnect.module.finance.repository.PaiementRepository;
import com.ossanasur.cbconnect.module.finance.repository.PrefinancementRemboursementRepository;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaiementImputationServiceImplTest {

    @Mock private PaiementImputationRepository imputationRepository;
    @Mock private EncaissementRepository encaissementRepository;
    @Mock private PaiementRepository paiementRepository;
    @Mock private PrefinancementRemboursementRepository prefiRemboursementRepository;
    @Mock private PaiementImputationMapper mapper;

    @InjectMocks private PaiementImputationServiceImpl service;

    private Sinistre sinistre;
    private Encaissement encaissement;
    private Paiement paiement;

    @BeforeEach
    void setUp() {
        sinistre = new Sinistre();
        sinistre.setSinistreTrackingId(UUID.randomUUID());
        sinistre.setHistoriqueId(1);

        encaissement = new Encaissement();
        encaissement.setEncaissementTrackingId(UUID.randomUUID());
        encaissement.setHistoriqueId(10);
        encaissement.setMontantCheque(new BigDecimal("100000.00"));
        encaissement.setNumeroCheque("CHQ-001");
        encaissement.setSinistre(sinistre);

        paiement = new Paiement();
        paiement.setPaiementTrackingId(UUID.randomUUID());
        paiement.setHistoriqueId(20);
        paiement.setMontant(new BigDecimal("75000.00"));
        paiement.setSinistre(sinistre);
    }

    // -------- getResteDisponible --------

    @Test
    void getResteDisponible_aucuneImputation_retourneMontantTotal() {
        when(encaissementRepository.findActiveByTrackingId(encaissement.getEncaissementTrackingId()))
                .thenReturn(Optional.of(encaissement));
        when(imputationRepository.sumImputationsByEncaissement(10)).thenReturn(BigDecimal.ZERO);
        when(prefiRemboursementRepository.sumMontantByEncaissement(10)).thenReturn(BigDecimal.ZERO);

        BigDecimal reste = service.getResteDisponible(encaissement.getEncaissementTrackingId());

        assertThat(reste).isEqualByComparingTo("100000.00");
    }

    @Test
    void getResteDisponible_avecImputationsActives_retourneCalculAlgebrique() {
        when(encaissementRepository.findActiveByTrackingId(encaissement.getEncaissementTrackingId()))
                .thenReturn(Optional.of(encaissement));
        when(imputationRepository.sumImputationsByEncaissement(10)).thenReturn(new BigDecimal("25000"));
        when(prefiRemboursementRepository.sumMontantByEncaissement(10)).thenReturn(BigDecimal.ZERO);

        BigDecimal reste = service.getResteDisponible(encaissement.getEncaissementTrackingId());

        assertThat(reste).isEqualByComparingTo("75000");
    }

    @Test
    void getResteDisponible_avecContrePassages_retourneSommeNulle() {
        // 25000 (origine) + (-25000) (contre-passage) = 0 net => sumImputations = 0 => reste = 100000
        when(encaissementRepository.findActiveByTrackingId(encaissement.getEncaissementTrackingId()))
                .thenReturn(Optional.of(encaissement));
        when(imputationRepository.sumImputationsByEncaissement(10)).thenReturn(BigDecimal.ZERO);
        when(prefiRemboursementRepository.sumMontantByEncaissement(10)).thenReturn(BigDecimal.ZERO);

        BigDecimal reste = service.getResteDisponible(encaissement.getEncaissementTrackingId());

        assertThat(reste).isEqualByComparingTo("100000.00");
    }

    @Test
    void getResteDisponible_avecPrefiRembourse_deduitLeRemboursement() {
        when(encaissementRepository.findActiveByTrackingId(encaissement.getEncaissementTrackingId()))
                .thenReturn(Optional.of(encaissement));
        when(imputationRepository.sumImputationsByEncaissement(10)).thenReturn(BigDecimal.ZERO);
        when(prefiRemboursementRepository.sumMontantByEncaissement(10)).thenReturn(new BigDecimal("60000"));

        BigDecimal reste = service.getResteDisponible(encaissement.getEncaissementTrackingId());

        assertThat(reste).isEqualByComparingTo("40000");
    }

    // -------- creerImputations --------

    @Test
    void creerImputations_avecMontantTotalCorrect_creeLignesPositives() {
        when(encaissementRepository.findActiveByTrackingIdForUpdate(any()))
                .thenReturn(Optional.of(encaissement));
        when(imputationRepository.sumImputationsByEncaissement(10)).thenReturn(BigDecimal.ZERO);
        when(prefiRemboursementRepository.sumMontantByEncaissement(10)).thenReturn(BigDecimal.ZERO);

        List<ImputationRequest> imputations = List.of(
                new ImputationRequest(encaissement.getEncaissementTrackingId(), new BigDecimal("75000")));

        service.creerImputations(paiement, imputations, "test-user");

        verify(imputationRepository, times(1)).save(any(PaiementImputation.class));
    }

    @Test
    void creerImputations_listeVide_throwsBadRequest() {
        assertThatThrownBy(() -> service.creerImputations(paiement, List.of(), "test-user"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Au moins une imputation");
    }

    @Test
    void creerImputations_avecSommeIncorrecte_throwsBadRequest() {
        List<ImputationRequest> imputations = List.of(
                new ImputationRequest(encaissement.getEncaissementTrackingId(), new BigDecimal("50000")));

        assertThatThrownBy(() -> service.creerImputations(paiement, imputations, "test-user"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("doit égaler le montant");
    }

    @Test
    void creerImputations_avecMontantZero_throwsBadRequest() {
        // Σ = 0 ≠ 75000 → rejet par le check de Σ
        List<ImputationRequest> imputations = List.of(
                new ImputationRequest(encaissement.getEncaissementTrackingId(), BigDecimal.ZERO));

        assertThatThrownBy(() -> service.creerImputations(paiement, imputations, "test-user"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void creerImputations_avecDepassementReste_throwsBadRequest() {
        when(encaissementRepository.findActiveByTrackingIdForUpdate(any()))
                .thenReturn(Optional.of(encaissement));
        when(imputationRepository.sumImputationsByEncaissement(10)).thenReturn(new BigDecimal("80000"));
        when(prefiRemboursementRepository.sumMontantByEncaissement(10)).thenReturn(BigDecimal.ZERO);

        // Reste = 100k - 80k = 20k. On demande 75k.
        List<ImputationRequest> imputations = List.of(
                new ImputationRequest(encaissement.getEncaissementTrackingId(), new BigDecimal("75000")));

        assertThatThrownBy(() -> service.creerImputations(paiement, imputations, "test-user"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Reste disponible insuffisant");
    }

    @Test
    void creerImputations_surEncaissementAutreSinistre_throwsBadRequest() {
        Sinistre autreSinistre = new Sinistre();
        autreSinistre.setSinistreTrackingId(UUID.randomUUID());
        encaissement.setSinistre(autreSinistre);

        when(encaissementRepository.findActiveByTrackingIdForUpdate(any()))
                .thenReturn(Optional.of(encaissement));

        List<ImputationRequest> imputations = List.of(
                new ImputationRequest(encaissement.getEncaissementTrackingId(), new BigDecimal("75000")));

        assertThatThrownBy(() -> service.creerImputations(paiement, imputations, "test-user"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("n'appartient pas au même sinistre");
    }

    // -------- contrePasserImputations --------

    @Test
    void contrePasserImputations_genereLignesNegativesSymetriques() {
        Paiement an = new Paiement();
        an.setHistoriqueId(30);
        an.setStatut(StatutPaiement.ANNULE);

        PaiementImputation origine = PaiementImputation.builder()
                .paiement(paiement)
                .encaissement(encaissement)
                .montantImpute(new BigDecimal("75000"))
                .activeData(true)
                .deletedData(false)
                .build();
        origine.setHistoriqueId(100);

        when(imputationRepository.findActiveByPaiement(paiement.getHistoriqueId()))
                .thenReturn(List.of(origine));

        service.contrePasserImputations(paiement, an, "test-user");

        verify(imputationRepository).save(argThat(pi ->
                pi.getMontantImpute().compareTo(new BigDecimal("-75000")) == 0
                && pi.getImputationOrigine() == origine
                && pi.getPaiement() == an));
    }

    @Test
    void contrePasserImputations_paiementANNonANNULE_throwsIllegalState() {
        Paiement nonAn = new Paiement();
        nonAn.setStatut(StatutPaiement.EMIS);

        assertThatThrownBy(() -> service.contrePasserImputations(paiement, nonAn, "test-user"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void contrePasserImputations_aucuneImputation_neFaitRien() {
        Paiement an = new Paiement();
        an.setStatut(StatutPaiement.ANNULE);

        when(imputationRepository.findActiveByPaiement(paiement.getHistoriqueId()))
                .thenReturn(List.of());

        service.contrePasserImputations(paiement, an, "test-user");

        verify(imputationRepository, never()).save(any());
    }
}
