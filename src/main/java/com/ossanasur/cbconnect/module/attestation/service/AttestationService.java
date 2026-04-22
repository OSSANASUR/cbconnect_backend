package com.ossanasur.cbconnect.module.attestation.service;
import com.ossanasur.cbconnect.module.attestation.dto.request.*;
import com.ossanasur.cbconnect.module.attestation.dto.response.*;
import com.ossanasur.cbconnect.utils.DataResponse;
import com.ossanasur.cbconnect.utils.PaginatedResponse;
import java.util.List;
import java.util.UUID;
public interface AttestationService {
    // Commandes
    DataResponse<CommandeAttestationResponse> passerCommande(CommandeAttestationRequest r, String loginAuteur);
    DataResponse<CommandeAttestationResponse> getCommande(UUID commandeId);
    PaginatedResponse<CommandeAttestationResponse> getAllCommandes(int page, int size);
    PaginatedResponse<CommandeAttestationResponse> getCommandesByOrganisme(UUID orgId, int page, int size);
    DataResponse<FactureAttestationResponse> genererProforma(UUID commandeId, String loginAuteur);
    DataResponse<FactureAttestationResponse> genererFactureDefinitive(UUID commandeId, String loginAuteur);
    DataResponse<Void> marquerLivre(UUID commandeId, java.time.LocalDate dateLivraison, String loginAuteur);
    DataResponse<Void> annulerCommande(UUID commandeId, String loginAuteur);
    DataResponse<Void> solderCommande(UUID commandeId, String loginAuteur);

    // Lots
    DataResponse<LotResponse> creerLot(LotRequest r, String loginAuteur);
    DataResponse<LotResponse> getLot(UUID lotId);
    PaginatedResponse<LotResponse> getAllLots(int page, int size);
    DataResponse<List<LotResponse>> getLotsDisponibles();

    // Chèques
    DataResponse<ChequeResponse> enregistrerCheque(ChequeRequest r, String loginAuteur);
    DataResponse<ChequeResponse> getCheque(UUID chequeId);
    PaginatedResponse<ChequeResponse> getAllCheques(int page, int size);
    DataResponse<List<ChequeResponse>> getChequesByCommande(UUID commandeId);
    DataResponse<Void> encaisserCheque(UUID chequeId, java.time.LocalDate dateEncaissement, String loginAuteur);
    DataResponse<Void> annulerCheque(UUID chequeId, String motif, String loginAuteur);

    // Factures
    DataResponse<FactureAttestationResponse> getFacture(UUID factureId);
    PaginatedResponse<FactureAttestationResponse> getAllFactures(int page, int size);
    DataResponse<List<FactureAttestationResponse>> getFacturesByCommande(UUID commandeId);

    // Tranches de livraison
    DataResponse<TrancheLivraisonResponse> livrerTranche(UUID commandeId, TrancheLivraisonRequest r, String loginAuteur);
    DataResponse<List<TrancheLivraisonResponse>> getTranchesByCommande(UUID commandeId);
}
