package com.ossanasur.cbconnect.module.delai.service;
import com.ossanasur.cbconnect.module.delai.dto.response.NotificationDelaiResponse;
import com.ossanasur.cbconnect.utils.DataResponse;
import java.util.List; import java.util.UUID;
public interface DelaiService {
    /** Déclenche tous les suivis de délai pour un sinistre nouvellement créé */
    void initialiserDelaisPourSinistre(UUID sinistreId, String loginAuteur);
    /** Recalcule les niveaux d'alerte et envoie les notifications graduées */
    void recalculerAlertes();
    /** Marquer un suivi de délai comme résolu */
    DataResponse<Void> resoudre(Integer notificationId, String motif, String loginAuteur);
    DataResponse<List<NotificationDelaiResponse>> getActiveBySinistre(UUID sinistreId);
    DataResponse<List<NotificationDelaiResponse>> getMesAlertes(String loginEmail);
    DataResponse<List<NotificationDelaiResponse>> getUrgents();
}
