package com.ossanasur.cbconnect.module.comptabilite.service;
import com.ossanasur.cbconnect.common.enums.TypeTransactionComptable;
import com.ossanasur.cbconnect.module.comptabilite.dto.response.EcritureResponse;
import com.ossanasur.cbconnect.utils.DataResponse;
import com.ossanasur.cbconnect.utils.PaginatedResponse;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.List; import java.util.UUID;
public interface ComptabiliteService {
    EcritureResponse genererEcritureAuto(TypeTransactionComptable type, UUID sinistreId, BigDecimal montant, String ref, String loginAuteur);
    DataResponse<EcritureResponse> validerEcriture(UUID ecritureId, String loginAuteur);
    DataResponse<List<EcritureResponse>> getBySinistre(UUID sinistreId);
    PaginatedResponse<EcritureResponse> getByPeriode(LocalDate debut, LocalDate fin, int page, int size);
}
