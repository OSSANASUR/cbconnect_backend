package com.ossanasur.cbconnect.module.indemnisation.service;

import com.ossanasur.cbconnect.module.indemnisation.dto.request.*;
import com.ossanasur.cbconnect.module.indemnisation.dto.response.*;
import com.ossanasur.cbconnect.utils.DataResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface IndemnisationService {
    DataResponse<OffreIndemnisationResponse> calculerOffre(UUID victimeId, CalculRequest params, String loginAuteur);

    DataResponse<OffreIndemnisationResponse> getOffreByVictime(UUID victimeId);

    DataResponse<OffreIndemnisationResponse> validerOffre(UUID offreId, String loginAuteur);

    DataResponse<BigDecimal> calculerPenalites(UUID offreId);

    DataResponse<AyantDroitResponse> ajouterAyantDroit(AyantDroitRequest r, String loginAuteur);

    DataResponse<List<AyantDroitResponse>> getAyantsDroitByVictime(UUID victimeId);

    DataResponse<Void> supprimerAyantDroit(UUID ayantDroitId, String loginAuteur);
}