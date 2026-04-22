package com.ossanasur.cbconnect.module.reclamation.service;
import com.ossanasur.cbconnect.common.enums.StatutDossierReclamation;
import com.ossanasur.cbconnect.module.reclamation.dto.request.*;
import com.ossanasur.cbconnect.module.reclamation.dto.response.*;
import com.ossanasur.cbconnect.utils.DataResponse;
import com.ossanasur.cbconnect.utils.PaginatedResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;
public interface ReclamationService {
    DataResponse<DossierReclamationResponse> ouvrirDossier(DossierReclamationRequest r, String loginAuteur);
    DataResponse<DossierReclamationResponse> getDossier(UUID dossierId);
    DataResponse<List<DossierReclamationResponse>> getDossiersByVictime(UUID victimeId);
    PaginatedResponse<DossierReclamationResponse> listerDossiers(
            StatutDossierReclamation statut, String search, int page, int size);

    DataResponse<FactureReclamationResponse> ajouterFacture(FactureReclamationRequest r, String loginAuteur);
    DataResponse<FactureReclamationResponse> ajouterFactureDansDossier(
            UUID dossierId, FactureReclamationRequest r, String loginAuteur);
    DataResponse<List<FactureReclamationResponse>> getFacturesByDossier(UUID dossierId);
    DataResponse<FactureReclamationResponse> updateFacture(UUID factureId, UpdateFactureRequest req, String loginAuteur);
    DataResponse<Void> supprimerFacture(UUID factureId, String loginAuteur);
    DataResponse<FactureReclamationResponse> attacherDocumentFacture(
            UUID factureId, MultipartFile file, String titre, String typeDocument, String loginAuteur);

    DataResponse<Void> clotureDossier(UUID dossierId, String loginAuteur);
}
