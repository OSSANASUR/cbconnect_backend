package com.ossanasur.cbconnect.module.finance.service.impl;

import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.module.finance.dto.request.ImputationRequest;
import com.ossanasur.cbconnect.module.finance.dto.response.EncaissementResteResponse;
import com.ossanasur.cbconnect.module.finance.dto.response.PaiementImputationResponse;
import com.ossanasur.cbconnect.module.finance.entity.Encaissement;
import com.ossanasur.cbconnect.module.finance.entity.Paiement;
import com.ossanasur.cbconnect.module.finance.mapper.PaiementImputationMapper;
import com.ossanasur.cbconnect.module.finance.repository.EncaissementRepository;
import com.ossanasur.cbconnect.module.finance.repository.PaiementImputationRepository;
import com.ossanasur.cbconnect.module.finance.repository.PaiementRepository;
import com.ossanasur.cbconnect.module.finance.repository.PrefinancementRemboursementRepository;
import com.ossanasur.cbconnect.module.finance.service.PaiementImputationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaiementImputationServiceImpl implements PaiementImputationService {

    private final PaiementImputationRepository imputationRepository;
    private final EncaissementRepository encaissementRepository;
    private final PaiementRepository paiementRepository;
    private final PrefinancementRemboursementRepository prefiRemboursementRepository;
    private final PaiementImputationMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getResteDisponible(UUID encaissementTrackingId) {
        Encaissement enc = encaissementRepository.findActiveByTrackingId(encaissementTrackingId)
                .orElseThrow(() -> new RessourceNotFoundException("Encaissement introuvable"));

        BigDecimal totalImpute = nz(imputationRepository.sumImputationsByEncaissement(enc.getHistoriqueId()));
        BigDecimal totalRemboursePrefi = nz(prefiRemboursementRepository.sumMontantByEncaissement(enc.getHistoriqueId()));

        return enc.getMontantCheque().subtract(totalImpute).subtract(totalRemboursePrefi);
    }

    @Override
    @Transactional(readOnly = true)
    public EncaissementResteResponse getResteDetailEncaissement(UUID encaissementTrackingId) {
        Encaissement enc = encaissementRepository.findActiveByTrackingId(encaissementTrackingId)
                .orElseThrow(() -> new RessourceNotFoundException("Encaissement introuvable"));

        BigDecimal totalImpute = nz(imputationRepository.sumImputationsByEncaissement(enc.getHistoriqueId()));
        BigDecimal totalRemboursePrefi = nz(prefiRemboursementRepository.sumMontantByEncaissement(enc.getHistoriqueId()));
        BigDecimal reste = enc.getMontantCheque().subtract(totalImpute).subtract(totalRemboursePrefi);

        List<PaiementImputationResponse> imputations = imputationRepository
                .findActiveByEncaissement(enc.getHistoriqueId())
                .stream().map(mapper::toResponse).toList();

        return new EncaissementResteResponse(
                enc.getEncaissementTrackingId(),
                enc.getNumeroCheque(),
                enc.getMontantCheque(),
                totalImpute.add(totalRemboursePrefi),
                reste,
                imputations);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaiementImputationResponse> getImputationsByEncaissement(UUID encaissementTrackingId) {
        Encaissement enc = encaissementRepository.findActiveByTrackingId(encaissementTrackingId)
                .orElseThrow(() -> new RessourceNotFoundException("Encaissement introuvable"));
        return imputationRepository.findActiveByEncaissement(enc.getHistoriqueId())
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaiementImputationResponse> getImputationsByPaiement(UUID paiementTrackingId) {
        Paiement p = paiementRepository.findActiveByTrackingId(paiementTrackingId)
                .orElseThrow(() -> new RessourceNotFoundException("Paiement introuvable"));
        return imputationRepository.findActiveByPaiement(p.getHistoriqueId())
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional
    public void creerImputations(Paiement paiement, List<ImputationRequest> imputations, String createdBy) {
        // À implémenter en Task B5
        throw new UnsupportedOperationException("À implémenter en Task B5");
    }

    @Override
    @Transactional
    public void contrePasserImputations(Paiement paiementOrigine, Paiement anPaiement, String createdBy) {
        // À implémenter en Task B6
        throw new UnsupportedOperationException("À implémenter en Task B6");
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
