package com.ossanasur.cbconnect.module.baremes.service;

import com.ossanasur.cbconnect.module.baremes.dto.request.*;
import com.ossanasur.cbconnect.module.baremes.dto.response.*;
import com.ossanasur.cbconnect.utils.DataResponse;
import java.util.List;

public interface BaremesService {
    // Capitalisation
    DataResponse<List<BaremeCapitalisationResponse>> listCapitalisation();
    DataResponse<BaremeCapitalisationResponse> createCapitalisation(BaremeCapitalisationRequest r);
    DataResponse<BaremeCapitalisationResponse> updateCapitalisation(Integer id, BaremeCapitalisationRequest r);
    DataResponse<Void> deleteCapitalisation(Integer id);

    // Valeur point IP
    DataResponse<List<BaremeValeurPointIpResponse>> listValeurPointIp();
    DataResponse<BaremeValeurPointIpResponse> createValeurPointIp(BaremeValeurPointIpRequest r);
    DataResponse<BaremeValeurPointIpResponse> updateValeurPointIp(Integer id, BaremeValeurPointIpRequest r);
    DataResponse<Void> deleteValeurPointIp(Integer id);

    // Clé répartition 265
    DataResponse<List<BaremeCleRepartition265Response>> listCleRepartition();
    DataResponse<BaremeCleRepartition265Response> createCleRepartition(BaremeCleRepartition265Request r);
    DataResponse<BaremeCleRepartition265Response> updateCleRepartition(Integer id, BaremeCleRepartition265Request r);
    DataResponse<Void> deleteCleRepartition(Integer id);

    // Préjudice moral 266
    DataResponse<List<BaremePrejudiceMoral266Response>> listPrejudiceMoral();
    DataResponse<BaremePrejudiceMoral266Response> createPrejudiceMoral(BaremePrejudiceMoral266Request r);
    DataResponse<BaremePrejudiceMoral266Response> updatePrejudiceMoral(Integer id, BaremePrejudiceMoral266Request r);
    DataResponse<Void> deletePrejudiceMoral(Integer id);

    // Pretium doloris / préjudice esthétique (art. 262)
    DataResponse<List<BaremePretiumDolorisResponse>> listPretiumDoloris();
    DataResponse<BaremePretiumDolorisResponse> createPretiumDoloris(BaremePretiumDolorisRequest r);
    DataResponse<BaremePretiumDolorisResponse> updatePretiumDoloris(Integer id, BaremePretiumDolorisRequest r);
    DataResponse<Void> deletePretiumDoloris(Integer id);
}
