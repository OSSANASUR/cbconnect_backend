package com.ossanasur.cbconnect.module.finance.service.impl;

import com.ossanasur.cbconnect.common.enums.TypeOperationFinanciere;
import com.ossanasur.cbconnect.module.finance.repository.PaiementRepository;
import com.ossanasur.cbconnect.module.finance.repository.PrefinancementRepository;
import com.ossanasur.cbconnect.module.finance.service.NumeroOperationGenerator;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class NumeroOperationGeneratorImpl implements NumeroOperationGenerator {

    private final PaiementRepository paiementRepository;
    private final PrefinancementRepository prefinancementRepository;

    @Override
    public String genererNumero(TypeOperationFinanciere type, Sinistre sinistre) {
        String code = type.getCode();
        String yyyy = String.valueOf(LocalDate.now().getYear());
        String numSin = resolveNumSinistre(sinistre);
        String prefix = "%s-%s-%s-".formatted(code, yyyy, numSin);

        long count = (type == TypeOperationFinanciere.PREFINANCEMENT)
                ? prefinancementRepository.countSeqForPrefinancement(prefix + "%")
                : paiementRepository.countSeqForTypeOnPaiement(prefix + "%");

        int seq = (int) count + 1;
        return prefix + "%03d".formatted(seq);
    }

    private String resolveNumSinistre(Sinistre s) {
        String raw = s.getNumeroSinistreLocal();
        if (!StringUtils.hasText(raw)) {
            raw = s.getNumeroSinistreManuel();
        }
        if (!StringUtils.hasText(raw)) {
            throw new IllegalStateException(
                    "Sinistre " + s.getHistoriqueId()
                            + " n'a ni numeroSinistreLocal ni numeroSinistreManuel — "
                            + "numéro d'opération impossible à générer");
        }
        return raw.toUpperCase().replaceAll("[^A-Z0-9]", "");
    }
}
