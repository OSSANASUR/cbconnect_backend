package com.ossanasur.cbconnect.module.delai.service;

import com.ossanasur.cbconnect.module.delai.dto.request.ParametreDelaiUpdateRequest;
import com.ossanasur.cbconnect.module.delai.dto.request.ParametreSystemeUpdateRequest;
import com.ossanasur.cbconnect.module.delai.dto.response.NotificationDelaiResponse;
import com.ossanasur.cbconnect.module.delai.dto.response.ParametreDelaiResponse;
import com.ossanasur.cbconnect.module.delai.dto.response.ParametreSystemeResponse;
import com.ossanasur.cbconnect.utils.DataResponse;
import java.util.List;
import java.util.UUID;

public interface DelaiService {
    void initialiserDelaisPourSinistre(UUID sinistreId, String loginAuteur);
    void recalculerAlertes();
    DataResponse<Void> resoudre(Integer notificationId, String motif, String loginAuteur);
    DataResponse<Void> relancerManuellement(Integer notificationId);
    DataResponse<List<NotificationDelaiResponse>> getActiveBySinistre(UUID sinistreId);
    DataResponse<List<NotificationDelaiResponse>> getMesAlertes(String loginEmail);
    DataResponse<List<NotificationDelaiResponse>> getUrgents();

    // Référentiel délais
    DataResponse<List<ParametreDelaiResponse>> listParametres();
    DataResponse<ParametreDelaiResponse> updateParametre(Integer id, ParametreDelaiUpdateRequest r);

    // Paramètres système (frais de gestion, etc.)
    DataResponse<List<ParametreSystemeResponse>> listParametresSysteme();
    DataResponse<ParametreSystemeResponse> updateParametreSysteme(Integer id, ParametreSystemeUpdateRequest r);
}
