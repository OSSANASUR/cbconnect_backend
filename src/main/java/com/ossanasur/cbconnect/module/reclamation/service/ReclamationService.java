package com.ossanasur.cbconnect.module.reclamation.service;
import com.ossanasur.cbconnect.module.reclamation.dto.request.*;
import com.ossanasur.cbconnect.module.reclamation.dto.response.*;
import com.ossanasur.cbconnect.utils.DataResponse;
import java.math.BigDecimal; import java.util.List; import java.util.UUID;
public interface ReclamationService {
    DataResponse<DossierReclamationResponse> ouvrirDossier(DossierReclamationRequest r, String loginAuteur);
    DataResponse<DossierReclamationResponse> getDossier(UUID dossierId);
    DataResponse<List<DossierReclamationResponse>> getDossiersByVictime(UUID victimeId);
    DataResponse<FactureReclamationResponse> ajouterFacture(FactureReclamationRequest r, String loginAuteur);
    DataResponse<FactureReclamationResponse> validerFacture(UUID factureId, BigDecimal montantRetenu, String loginAuteur);
    DataResponse<FactureReclamationResponse> rejeterFacture(UUID factureId, String motif, String loginAuteur);
    DataResponse<List<FactureReclamationResponse>> getFacturesByDossier(UUID dossierId);
    DataResponse<Void> clotureDossier(UUID dossierId, String loginAuteur);
}
