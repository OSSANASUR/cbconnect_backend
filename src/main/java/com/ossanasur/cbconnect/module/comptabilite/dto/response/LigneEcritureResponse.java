package com.ossanasur.cbconnect.module.comptabilite.dto.response;
import java.math.BigDecimal;
public record LigneEcritureResponse(
    String numeroCompte, String libelleCompte, String sens,
    BigDecimal montant, String libelleLigne
) {}
