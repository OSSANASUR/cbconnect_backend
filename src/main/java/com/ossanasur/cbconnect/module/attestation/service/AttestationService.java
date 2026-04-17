package com.ossanasur.cbconnect.module.attestation.service;
import com.ossanasur.cbconnect.module.attestation.dto.request.*;
import com.ossanasur.cbconnect.module.attestation.dto.response.*;
import com.ossanasur.cbconnect.utils.DataResponse;
import com.ossanasur.cbconnect.utils.PaginatedResponse;
import java.util.UUID;
public interface AttestationService {
    DataResponse<CommandeAttestationResponse> passerCommande(CommandeAttestationRequest r, String loginAuteur);
    DataResponse<CommandeAttestationResponse> getCommande(UUID commandeId);
    PaginatedResponse<CommandeAttestationResponse> getAllCommandes(int page, int size);
    PaginatedResponse<CommandeAttestationResponse> getCommandesByOrganisme(UUID orgId, int page, int size);
    DataResponse<FactureAttestationResponse> genererProforma(UUID commandeId, String loginAuteur);
    DataResponse<FactureAttestationResponse> genererFactureDefinitive(UUID commandeId, String loginAuteur);
    DataResponse<Void> marquerLivre(UUID commandeId, java.time.LocalDate dateLivraison, String loginAuteur);
    DataResponse<Void> annulerCommande(UUID commandeId, String loginAuteur);
}
