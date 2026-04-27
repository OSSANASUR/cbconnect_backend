package com.ossanasur.cbconnect.module.finance.service.impl;

import com.ossanasur.cbconnect.exception.BadRequestException;
import com.ossanasur.cbconnect.module.finance.repository.EncaissementRepository;
import com.ossanasur.cbconnect.module.finance.repository.PaiementRepository;
import com.ossanasur.cbconnect.module.finance.service.EncaissementGuardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EncaissementGuardServiceImpl implements EncaissementGuardService {

    private final EncaissementRepository encaissementRepository;
    private final PaiementRepository paiementRepository;

    @Override
    @Transactional(readOnly = true)
    public void verifierRegleA(UUID sinistreTrackingId) {
        if (!encaissementRepository.existsNonAnnuleBySinistre(sinistreTrackingId)) {
            throw new BadRequestException(
                    "Impossible d'enregistrer un règlement : aucun encaissement n'a été déclaré pour ce sinistre.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void verifierRegleB(UUID sinistreTrackingId) {
        BigDecimal somme = encaissementRepository.sumMontantEncaisseBySinistre(sinistreTrackingId);
        if (somme == null || somme.signum() <= 0) {
            throw new BadRequestException(
                    "Le règlement comptable est bloqué : aucun chèque d'encaissement n'a encore été crédité en banque.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void verifierRegleC(UUID sinistreTrackingId, BigDecimal montantNouveau) {
        throw new UnsupportedOperationException("À implémenter task 7");
    }
}
