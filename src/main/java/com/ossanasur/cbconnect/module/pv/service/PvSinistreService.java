package com.ossanasur.cbconnect.module.pv.service;
import com.ossanasur.cbconnect.module.pv.dto.request.PvSinistreRequest;
import com.ossanasur.cbconnect.module.pv.dto.response.PvSinistreResponse;
import com.ossanasur.cbconnect.utils.DataResponse;
import com.ossanasur.cbconnect.utils.PaginatedResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import java.util.List; import java.util.UUID;
public interface PvSinistreService {
    DataResponse<PvSinistreResponse> enregistrer(PvSinistreRequest r, String loginAuteur);
    DataResponse<PvSinistreResponse> modifier(UUID id, PvSinistreRequest r, String loginAuteur);
    DataResponse<PvSinistreResponse> getByTrackingId(UUID id);
    DataResponse<List<PvSinistreResponse>> getBySinistre(UUID sinistreId);
    DataResponse<List<PvSinistreResponse>> getNonAssocies();
    PaginatedResponse<PvSinistreResponse> lister(int page, int size, Boolean estComplet);
    DataResponse<Void> associerSinistre(UUID pvId, UUID sinistreId, String loginAuteur);
    DataResponse<Void> supprimer(UUID id, String loginAuteur);
    DataResponse<PvSinistreResponse> attacherDocument(UUID pvTrackingId, MultipartFile file, String titre, String typeDocument, String loginAuteur);
    ResponseEntity<Resource> telechargerDocument(UUID pvTrackingId);
}
