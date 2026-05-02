package com.ossanasur.cbconnect.module.finance.service.impl;

import com.ossanasur.cbconnect.exception.BadRequestException;
import com.ossanasur.cbconnect.module.finance.repository.EncaissementRepository;
import com.ossanasur.cbconnect.module.finance.repository.PaiementRepository;
import com.ossanasur.cbconnect.module.finance.repository.PrefinancementRepository;
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
    private final PrefinancementRepository prefinancementRepository;

    @Override
    @Transactional(readOnly = true)
    public void verifierRegleA(UUID sinistreTrackingId) {
        boolean hasEnc = encaissementRepository.existsActifNonAnnuleBySinistre(sinistreTrackingId);
        boolean hasPref = prefinancementRepository.existsActifBySinistre(sinistreTrackingId);
        if (!hasEnc && !hasPref) {
            throw new BadRequestException(
                    "Règle A non satisfaite : aucun encaissement non-annulé ni préfinancement actif sur ce sinistre.");
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
        BigDecimal nouveau = montantNouveau == null ? BigDecimal.ZERO : montantNouveau;
        BigDecimal encaisse = notZero(encaissementRepository.sumMontantActifBySinistre(sinistreTrackingId));
        BigDecimal prefiActif = notZero(prefinancementRepository.sumMontantActifBySinistre(sinistreTrackingId));
        BigDecimal engage = notZero(paiementRepository.sumMontantActifBySinistre(sinistreTrackingId));

        // Solde réel = encaissé − préfi (déjà décaissés) − engagements actuels
        BigDecimal soldeAvantNouveau = encaisse.subtract(prefiActif).subtract(engage);

        if (soldeAvantNouveau.compareTo(nouveau) < 0) {
            BigDecimal manque = nouveau.subtract(soldeAvantNouveau);
            throw new BadRequestException(String.format(
                    "Couverture financière insuffisante : encaissé %s − préfinancements %s − engagés %s = %s FCFA, "
                    + "ce règlement %s FCFA (manque %s FCFA).",
                    encaisse.toPlainString(), prefiActif.toPlainString(), engage.toPlainString(),
                    soldeAvantNouveau.toPlainString(), nouveau.toPlainString(), manque.toPlainString()));
        }
    }

    private static BigDecimal notZero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
