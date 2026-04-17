package com.ossanasur.cbconnect.module.ged.service;
import com.ossanasur.cbconnect.module.ged.dto.request.UploadDocumentRequest;
import com.ossanasur.cbconnect.module.ged.dto.response.DocumentGedResponse;
import com.ossanasur.cbconnect.module.ged.dto.response.DossierGedResponse;
import com.ossanasur.cbconnect.utils.DataResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;
public interface PaperlessClientService {
    DataResponse<DossierGedResponse> creerDossierSinistre(UUID sinistreId, String loginAuteur);
    DataResponse<DossierGedResponse> creerDossierVictime(UUID victimeId, String loginAuteur);
    DataResponse<DocumentGedResponse> uploadDocument(MultipartFile file, UploadDocumentRequest r, String loginAuteur);
    DataResponse<byte[]> telechargerDocument(Integer paperlessDocumentId);
    DataResponse<List<DocumentGedResponse>> listerDocumentsSinistre(UUID sinistreId);
    DataResponse<List<DocumentGedResponse>> listerDocumentsVictime(UUID victimeId);
    void initTagsPaperless();
}
