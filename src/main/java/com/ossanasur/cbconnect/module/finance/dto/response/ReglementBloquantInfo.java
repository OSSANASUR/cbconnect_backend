package com.ossanasur.cbconnect.module.finance.dto.response;

import com.ossanasur.cbconnect.common.enums.StatutPaiement;
import com.ossanasur.cbconnect.module.finance.entity.Paiement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Vue compacte d'un règlement qui bloque l'annulation d'un encaissement.
 * Renvoyée dans le body 400 de PATCH /v1/encaissements/{id}/annuler.
 */
public record ReglementBloquantInfo(
        UUID paiementTrackingId,
        String numeroPaiement,
        String beneficiaire,
        BigDecimal montant,
        StatutPaiement statut,
        LocalDate dateEmission,
        String numeroChequeEmis
) {
    public static ReglementBloquantInfo fromPaiement(Paiement p) {
        return new ReglementBloquantInfo(
                p.getPaiementTrackingId(),
                p.getNumeroPaiement(),
                p.getBeneficiaire(),
                p.getMontant(),
                p.getStatut(),
                p.getDateEmission(),
                p.getNumeroChequeEmis()
        );
    }
}
