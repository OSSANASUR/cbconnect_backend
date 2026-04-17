package com.ossanasur.cbconnect.module.attestation.service.impl;
import com.ossanasur.cbconnect.common.enums.*;
import com.ossanasur.cbconnect.exception.*;
import com.ossanasur.cbconnect.module.attestation.dto.request.*;
import com.ossanasur.cbconnect.module.attestation.dto.response.*;
import com.ossanasur.cbconnect.module.attestation.entity.*;
import com.ossanasur.cbconnect.module.attestation.mapper.AttestationMapper;
import com.ossanasur.cbconnect.module.attestation.repository.*;
import com.ossanasur.cbconnect.module.attestation.service.AttestationService;
import com.ossanasur.cbconnect.module.auth.repository.OrganismeRepository;
import com.ossanasur.cbconnect.utils.DataResponse;
import com.ossanasur.cbconnect.utils.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
@Service @RequiredArgsConstructor
public class AttestationServiceImpl implements AttestationService {
    private final CommandeAttestationRepository commandeRepository;
    private final FactureAttestationRepository factureRepository;
    private final OrganismeRepository organismeRepository;
    private final AttestationMapper mapper;

    private static final BigDecimal PRIX_UNITAIRE = new BigDecimal("1075");
    private static final BigDecimal CONTRIBUTION_FONDS = new BigDecimal("100");

    @Override @Transactional
    public DataResponse<CommandeAttestationResponse> passerCommande(CommandeAttestationRequest r, String loginAuteur) {
        var organisme = organismeRepository.findActiveByTrackingId(r.organismeTrackingId())
            .orElseThrow(() -> new RessourceNotFoundException("Organisme introuvable"));
        if (!TypeOrganisme.COMPAGNIE_MEMBRE.equals(organisme.getTypeOrganisme()))
            throw new BadRequestException("Seules les compagnies membres peuvent commander des attestations");

        int annee = LocalDate.now().getYear();
        long seq = commandeRepository.findMaxSequenceByAnnee(annee) + 1;
        String numeroCommande = "CMD-" + annee + "-" + String.format("%03d", seq);

        BigDecimal montantAtt = PRIX_UNITAIRE.multiply(BigDecimal.valueOf(r.quantite()));
        BigDecimal montantFonds = CONTRIBUTION_FONDS.multiply(BigDecimal.valueOf(r.quantite()));
        BigDecimal total = montantAtt.add(montantFonds);

        CommandeAttestation c = CommandeAttestation.builder()
            .commandeTrackingId(UUID.randomUUID()).numeroCommande(numeroCommande)
            .statut(StatutCommandeAttestation.PROFORMA_EMISE).quantite(r.quantite())
            .prixUnitaireVente(PRIX_UNITAIRE).tauxContributionFonds(CONTRIBUTION_FONDS)
            .montantAttestation(montantAtt).montantContributionFonds(montantFonds).montantTotal(total)
            .nomBeneficiaireCheque(r.nomBeneficiaireCheque()).dateCommande(LocalDate.now())
            .organisme(organisme).createdBy(loginAuteur).activeData(true).deletedData(false)
            .fromTable(TypeTable.COMMANDE_ATTESTATION).build();
        return DataResponse.created("Commande " + numeroCommande + " creee", mapper.toCommandeResponse(commandeRepository.save(c)));
    }

    @Override @Transactional(readOnly=true)
    public DataResponse<CommandeAttestationResponse> getCommande(UUID id) {
        return DataResponse.success(mapper.toCommandeResponse(commandeRepository.findActiveByTrackingId(id)
            .orElseThrow(() -> new RessourceNotFoundException("Commande introuvable"))));
    }

    @Override @Transactional(readOnly=true)
    public PaginatedResponse<CommandeAttestationResponse> getAllCommandes(int page, int size) {
        return PaginatedResponse.fromPage(commandeRepository.findAllActive(
            PageRequest.of(page, size, Sort.by("dateCommande").descending()))
            .map(mapper::toCommandeResponse), "Toutes les commandes");
    }

    @Override @Transactional(readOnly=true)
    public PaginatedResponse<CommandeAttestationResponse> getCommandesByOrganisme(UUID orgId, int page, int size) {
        return PaginatedResponse.fromPage(commandeRepository.findByOrganisme(orgId,
            PageRequest.of(page, size, Sort.by("dateCommande").descending()))
            .map(mapper::toCommandeResponse), "Commandes organisme");
    }

    @Override @Transactional
    public DataResponse<FactureAttestationResponse> genererProforma(UUID commandeId, String loginAuteur) {
        return genererFacture(commandeId, TypeFactureAttestation.PROFORMA, loginAuteur);
    }

    @Override @Transactional
    public DataResponse<FactureAttestationResponse> genererFactureDefinitive(UUID commandeId, String loginAuteur) {
        return genererFacture(commandeId, TypeFactureAttestation.DEFINITIVE, loginAuteur);
    }

    private DataResponse<FactureAttestationResponse> genererFacture(UUID commandeId, TypeFactureAttestation type, String loginAuteur) {
        var commande = commandeRepository.findActiveByTrackingId(commandeId)
            .orElseThrow(() -> new RessourceNotFoundException("Commande introuvable"));
        int annee = LocalDate.now().getYear();
        long seq = factureRepository.findMaxSequenceByAnnee(annee) + 1;
        String numeroFacture = "BNCB-" + String.format("%03d", seq) + "/" + annee;
        FactureAttestation f = FactureAttestation.builder()
            .factureTrackingId(UUID.randomUUID()).numeroFacture(numeroFacture).typeFacture(type)
            .dateFacture(LocalDate.now()).montantAttestation(commande.getMontantAttestation())
            .montantContributionFonds(commande.getMontantContributionFonds()).montantTotal(commande.getMontantTotal())
            .instructionCheque(commande.getNomBeneficiaireCheque())
            .dateEcheance(LocalDate.now().plusDays(30)).annulee(false).commande(commande)
            .createdBy(loginAuteur).activeData(true).deletedData(false).fromTable(TypeTable.FACTURE_ATTESTATION).build();
        if (TypeFactureAttestation.PROFORMA.equals(type))
            commande.setStatut(StatutCommandeAttestation.PROFORMA_EMISE);
        else
            commande.setStatut(StatutCommandeAttestation.FACTURE_EMISE);
        commande.setUpdatedBy(loginAuteur); commandeRepository.save(commande);
        return DataResponse.created("Facture " + numeroFacture + " generee", mapper.toFactureResponse(factureRepository.save(f)));
    }

    @Override @Transactional
    public DataResponse<Void> marquerLivre(UUID commandeId, LocalDate dateLivraison, String loginAuteur) {
        var c = commandeRepository.findActiveByTrackingId(commandeId)
            .orElseThrow(() -> new RessourceNotFoundException("Commande introuvable"));
        c.setStatut(StatutCommandeAttestation.LIVRE); c.setDateLivraisonEffective(dateLivraison); c.setUpdatedBy(loginAuteur);
        commandeRepository.save(c);
        return DataResponse.success("Attestations marquees livrees", null);
    }

    @Override @Transactional
    public DataResponse<Void> annulerCommande(UUID commandeId, String loginAuteur) {
        var c = commandeRepository.findActiveByTrackingId(commandeId)
            .orElseThrow(() -> new RessourceNotFoundException("Commande introuvable"));
        c.setStatut(StatutCommandeAttestation.ANNULEE); c.setUpdatedBy(loginAuteur);
        commandeRepository.save(c);
        return DataResponse.success("Commande annulee", null);
    }
}
