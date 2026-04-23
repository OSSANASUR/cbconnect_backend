package com.ossanasur.cbconnect.module.ged.service;

import com.ossanasur.cbconnect.module.ged.dto.request.UploadDocumentRequest;
import com.ossanasur.cbconnect.module.ged.dto.response.DocumentGedResponse;
import com.ossanasur.cbconnect.module.ged.dto.response.DossierGedResponse;
import com.ossanasur.cbconnect.utils.DataResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

public interface OssanGedClientService {
    record GedTaskStatus(String taskId, String status, Integer documentId, String message) {}

    DataResponse<DossierGedResponse> creerDossierSinistre(UUID sinistreId, String loginAuteur);

    DataResponse<DossierGedResponse> creerDossierVictime(UUID victimeId, String loginAuteur);

    DataResponse<DossierGedResponse> creerDossierReclamation(UUID dossierReclamationId, String loginAuteur);

    DataResponse<DocumentGedResponse> uploadDocument(MultipartFile file, UploadDocumentRequest r, String loginAuteur);

    DataResponse<byte[]> telechargerDocument(Integer ossanGedDocumentId);

    DataResponse<List<DossierGedResponse>> arbreDossiers();

    DataResponse<String> repairerStoragePaths();

    DataResponse<String> migrerDocumentsStoragePaths();

    DataResponse<List<DocumentGedResponse>> listerDocumentsSinistre(UUID sinistreId);

    DataResponse<List<DocumentGedResponse>> listerDocumentsVictime(UUID victimeId);

    void initTagsOssanGed();

    GedTaskStatus consulterStatutTache(String taskId);

    DataResponse<DocumentGedResponse> resoudreDocument(UUID ossanGedDocumentTrackingId);

    void provisionnerUtilisateur(String username);

    String verifierSessionPaperless(String cookieHeader);
}
