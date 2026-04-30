package com.ossanasur.cbconnect.module.finance.exception;

import com.ossanasur.cbconnect.module.finance.entity.Paiement;
import lombok.Getter;

import java.util.List;

/**
 * Levée quand on tente d'annuler un Encaissement alors que des règlements
 * actifs y sont encore rattachés. Mappée 400 par GlobalExceptionHandler
 * avec la liste des règlements bloquants en payload.
 */
@Getter
public class ReglementsLiesNonAnnulesException extends RuntimeException {

    private final List<Paiement> reglementsBloquants;

    public ReglementsLiesNonAnnulesException(List<Paiement> reglementsBloquants) {
        super("Annulez d'abord les " + reglementsBloquants.size()
                + " règlement(s) lié(s) à cet encaissement");
        this.reglementsBloquants = List.copyOf(reglementsBloquants);
    }
}
