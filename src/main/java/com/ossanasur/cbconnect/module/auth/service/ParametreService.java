package com.ossanasur.cbconnect.module.auth.service;
import com.ossanasur.cbconnect.module.auth.dto.request.ParametreRequest;
import com.ossanasur.cbconnect.module.auth.dto.response.ParametreResponse;
import com.ossanasur.cbconnect.utils.DataResponse;
import com.ossanasur.cbconnect.utils.PaginatedResponse;
import java.util.List;
import java.util.UUID;
public interface ParametreService {
    DataResponse<ParametreResponse> create(ParametreRequest request, String loginAuteur);
    DataResponse<ParametreResponse> update(UUID trackingId, ParametreRequest request, String loginAuteur);
    DataResponse<ParametreResponse> getByTrackingId(UUID trackingId);
    DataResponse<ParametreResponse> getByCle(String cle);
    DataResponse<List<ParametreResponse>> getAll();
    DataResponse<List<ParametreResponse>> getByCategorie(String categorie);
    DataResponse<List<ParametreResponse>> getAllListe();
    DataResponse<Void> delete(UUID trackingId, String loginAuteur);
    PaginatedResponse<ParametreResponse> getHistory(UUID trackingId, int page, int size);

    /** Retourne la valeur active d'un parametre, ou la valeur par defaut si la cle n'existe pas. */
    String getValeur(String cle, String defaut);
}
