package com.ossanasur.cbconnect.module.courrier.service;

import com.ossanasur.cbconnect.common.enums.TypeRegistre;
import com.ossanasur.cbconnect.module.courrier.dto.request.RegistreJourRequest;
import com.ossanasur.cbconnect.module.courrier.dto.request.VisaRegistreRequest;
import com.ossanasur.cbconnect.module.courrier.dto.response.CourrierResponse;
import com.ossanasur.cbconnect.module.courrier.dto.response.RegistreJourResponse;
import com.ossanasur.cbconnect.module.courrier.entity.RegistreJour;
import com.ossanasur.cbconnect.utils.DataResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface RegistreJourService {

    DataResponse<RegistreJourResponse> ouvrir(RegistreJourRequest r, String loginAuteur);

    DataResponse<RegistreJourResponse> getByTrackingId(UUID id);

    DataResponse<RegistreJourResponse> getDuJour(LocalDate date, TypeRegistre type);

    DataResponse<List<RegistreJourResponse>> getAll();

    /** Clôture le registre (fin de journée). Ne peut plus accepter de nouveau courrier. */
    DataResponse<RegistreJourResponse> cloturer(UUID id, String loginAuteur);

    /** Visa du chef avec commentaire optionnel et scan signé. */
    DataResponse<RegistreJourResponse> viser(UUID id, VisaRegistreRequest r, String loginAuteur);

    DataResponse<List<CourrierResponse>> getCourriers(UUID id);

    /**
     * Helper interne : récupère (ou crée) le registre OUVERT du jour courant
     * pour attacher automatiquement un courrier enregistré à la volée.
     */
    RegistreJour getOuCreerRegistreOuvert(LocalDate date, TypeRegistre type, String loginAuteur);
}
