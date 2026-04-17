package com.ossanasur.cbconnect.module.indemnisation.dto.response;
import com.ossanasur.cbconnect.common.enums.LienParente;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.UUID;
public record AyantDroitResponse(
    UUID ayantDroitTrackingId, String nom, String prenoms,
    LocalDate dateNaissance, String sexe, LienParente lien,
    boolean estOrphelinDouble, boolean poursuiteEtudes,
    BigDecimal montantPe, BigDecimal montantPm, BigDecimal montantTotal,
    String victimeNomPrenom
) {}
