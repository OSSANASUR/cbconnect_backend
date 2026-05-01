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
import com.ossanasur.cbconnect.module.auth.repository.ParametreRepository;
import com.ossanasur.cbconnect.utils.DataResponse;
import com.ossanasur.cbconnect.utils.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
@Service @RequiredArgsConstructor
public class AttestationServiceImpl implements AttestationService {
    private final CommandeAttestationRepository commandeRepository;
    private final FactureAttestationRepository factureRepository;
    private final LotApprovisionnementRepository lotRepository;
    private final ChequeRecuAttestationRepository chequeRepository;
    private final TrancheLivraisonAttestationRepository trancheRepository;
    private final OrganismeRepository organismeRepository;
    private final ParametreRepository parametreRepository;
    private final AttestationMapper mapper;

    // Pas de fallback en dur : les tarifs sont configures dans Parametres > Attestations.
    // Si la cle est absente ou non parsable, on echoue tot et explicitement.
    private BigDecimal getMontantParamRequired(String cle) {
        var p = parametreRepository.findByCle(cle)
            .orElseThrow(() -> new BadRequestException(
                "Parametre tarifaire '" + cle + "' non configure. Renseignez-le dans Parametres > Attestations."));
        try {
            return new BigDecimal(p.getValeur());
        } catch (NumberFormatException e) {
            throw new BadRequestException(
                "Parametre '" + cle + "' a une valeur invalide ('" + p.getValeur() + "'). Corrigez-la dans Parametres > Attestations.");
        }
    }
    public BigDecimal getPrixUnitaire() { return getMontantParamRequired("PRIX_UNITAIRE_ATTESTATION_FCFA"); }
    public BigDecimal getContributionFonds() { return getMontantParamRequired("CONTRIBUTION_FONDS_ATTESTATION_FCFA"); }

    // ============== COMMANDES ==============

    @Override @Transactional
    public DataResponse<CommandeAttestationResponse> passerCommande(CommandeAttestationRequest r, String loginAuteur) {
        var organisme = organismeRepository.findActiveByTrackingId(r.organismeTrackingId())
            .orElseThrow(() -> new RessourceNotFoundException("Organisme introuvable"));
        if (!TypeOrganisme.COMPAGNIE_MEMBRE.equals(organisme.getTypeOrganisme()))
            throw new BadRequestException("Seules les compagnies membres peuvent commander des attestations");

        int annee = LocalDate.now().getYear();
        long seq = commandeRepository.findMaxSequenceByAnnee(annee) + 1;
        String numeroCommande = "CMD-" + annee + "-" + String.format("%03d", seq);

        BigDecimal prixUnitaire = getPrixUnitaire();
        BigDecimal contribution = getContributionFonds();
        BigDecimal montantAtt = prixUnitaire.multiply(BigDecimal.valueOf(r.quantite()));
        BigDecimal montantFonds = contribution.multiply(BigDecimal.valueOf(r.quantite()));
        BigDecimal total = montantAtt.add(montantFonds);

        CommandeAttestation c = CommandeAttestation.builder()
            .commandeTrackingId(UUID.randomUUID()).numeroCommande(numeroCommande)
            .statut(StatutCommandeAttestation.PROFORMA_EMISE).quantite(r.quantite())
            .prixUnitaireVente(prixUnitaire).tauxContributionFonds(contribution)
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
        var commande = commandeRepository.findActiveByTrackingId(commandeId)
            .orElseThrow(() -> new RessourceNotFoundException("Commande introuvable"));
        if (!StatutCommandeAttestation.LIVRE.equals(commande.getStatut())
            && !StatutCommandeAttestation.CHEQUE_RECU.equals(commande.getStatut()))
            throw new BadRequestException("La facture definitive ne peut etre emise qu'apres reception du cheque ou livraison");
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
        if (TypeFactureAttestation.DEFINITIVE.equals(type))
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

    @Override @Transactional
    public DataResponse<Void> solderCommande(UUID commandeId, String loginAuteur) {
        var c = commandeRepository.findActiveByTrackingId(commandeId)
            .orElseThrow(() -> new RessourceNotFoundException("Commande introuvable"));
        if (!StatutCommandeAttestation.FACTURE_EMISE.equals(c.getStatut())
            && !StatutCommandeAttestation.LIVRE.equals(c.getStatut()))
            throw new BadRequestException("Une commande ne peut etre soldee qu'apres facturation definitive");
        c.setStatut(StatutCommandeAttestation.SOLDE); c.setUpdatedBy(loginAuteur);
        commandeRepository.save(c);
        return DataResponse.success("Commande soldee", null);
    }

    // ============== LOTS ==============

    @Override @Transactional
    public DataResponse<LotResponse> creerLot(LotRequest r, String loginAuteur) {
        String reference = (r.referenceLot() != null && !r.referenceLot().isBlank())
            ? r.referenceLot().trim()
            : genererReferenceLot();
        if (lotRepository.existsByReferenceLot(reference))
            throw new AlreadyExistException("Un lot avec la reference '" + reference + "' existe deja");
        LotApprovisionnement l = LotApprovisionnement.builder()
            .lotTrackingId(UUID.randomUUID()).referenceLot(reference)
            .nomFournisseur(r.nomFournisseur() != null && !r.nomFournisseur().isBlank() ? r.nomFournisseur().trim() : null)
            .numeroBonCommande(r.numeroBonCommande()).quantite(r.quantite())
            .numeroDebutSerie(r.numeroDebutSerie()).numeroFinSerie(r.numeroFinSerie())
            .prixUnitaireAchat(r.prixUnitaireAchat()).dateCommande(r.dateCommande())
            .dateLivraisonFournisseur(r.dateLivraisonFournisseur())
            .statutLot(StatutLot.LIVRE)
            .createdBy(loginAuteur).activeData(true).deletedData(false)
            .fromTable(TypeTable.LOT_APPROVISIONNEMENT).build();
        return DataResponse.created("Lot " + reference + " enregistre",
            mapper.toLotResponse(lotRepository.save(l), 0L));
    }

    private String genererReferenceLot() {
        int annee = LocalDate.now().getYear();
        long seq = lotRepository.findMaxSequenceByAnnee(annee) + 1;
        return "LOT-" + annee + "-" + String.format("%03d", seq);
    }

    @Override @Transactional
    public DataResponse<LotResponse> modifierLot(UUID lotId, LotRequest r, String loginAuteur) {
        var l = lotRepository.findActiveByTrackingId(lotId)
            .orElseThrow(() -> new RessourceNotFoundException("Lot introuvable"));
        long dejaDistribue = trancheRepository.sommeLivreParLot(lotId);

        // Si le lot a déjà servi, on bloque la modification de la plage et de la quantité
        boolean plageOuQuantiteChange =
            !l.getNumeroDebutSerie().equals(r.numeroDebutSerie())
            || !l.getNumeroFinSerie().equals(r.numeroFinSerie())
            || !l.getQuantite().equals(r.quantite());
        if (dejaDistribue > 0 && plageOuQuantiteChange) {
            throw new BadRequestException("Ce lot a déjà servi à des livraisons (" + dejaDistribue
                + " attestations distribuées). La plage de série et la quantité ne peuvent plus être modifiées.");
        }
        if (r.quantite() < dejaDistribue) {
            throw new BadRequestException("La nouvelle quantité (" + r.quantite()
                + ") ne peut pas être inférieure à ce qui a déjà été distribué (" + dejaDistribue + ").");
        }

        if (r.nomFournisseur() != null && !r.nomFournisseur().isBlank()) l.setNomFournisseur(r.nomFournisseur().trim());
        else l.setNomFournisseur(null);
        l.setNumeroBonCommande(r.numeroBonCommande());
        l.setQuantite(r.quantite());
        l.setNumeroDebutSerie(r.numeroDebutSerie());
        l.setNumeroFinSerie(r.numeroFinSerie());
        l.setPrixUnitaireAchat(r.prixUnitaireAchat());
        l.setDateCommande(r.dateCommande());
        l.setDateLivraisonFournisseur(r.dateLivraisonFournisseur());
        l.setUpdatedBy(loginAuteur);
        return DataResponse.success("Lot " + l.getReferenceLot() + " modifié",
            mapper.toLotResponse(lotRepository.save(l), dejaDistribue));
    }

    @Override @Transactional(readOnly=true)
    public DataResponse<LotResponse> getLot(UUID lotId) {
        var l = lotRepository.findActiveByTrackingId(lotId)
            .orElseThrow(() -> new RessourceNotFoundException("Lot introuvable"));
        return DataResponse.success(mapper.toLotResponse(l, trancheRepository.sommeLivreParLot(lotId)));
    }

    @Override @Transactional(readOnly=true)
    public PaginatedResponse<LotResponse> getAllLots(int page, int size) {
        var p = lotRepository.findAllActive(PageRequest.of(page, size, Sort.by("dateCommande").descending()));
        return PaginatedResponse.fromPage(
            p.map(l -> mapper.toLotResponse(l, trancheRepository.sommeLivreParLot(l.getLotTrackingId()))),
            "Tous les lots"
        );
    }

    @Override @Transactional(readOnly=true)
    public DataResponse<List<LotResponse>> getLotsDisponibles() {
        var lots = lotRepository.findAllActive().stream()
            .filter(l -> StatutLot.LIVRE.equals(l.getStatutLot()))
            .map(l -> mapper.toLotResponse(l, trancheRepository.sommeLivreParLot(l.getLotTrackingId())))
            .filter(lr -> lr.quantiteRestante() > 0)
            .toList();
        return DataResponse.success("Lots disponibles", lots);
    }

    // ============== CHÈQUES ==============

    @Override @Transactional
    public DataResponse<ChequeResponse> enregistrerCheque(ChequeRequest r, String loginAuteur) {
        var facture = factureRepository.findActiveByTrackingId(r.factureTrackingId())
            .orElseThrow(() -> new RessourceNotFoundException("Facture introuvable"));
        if (facture.isAnnulee())
            throw new BadRequestException("Impossible d'enregistrer un cheque sur une facture annulee");
        ChequeRecuAttestation cheque = ChequeRecuAttestation.builder()
            .chequeTrackingId(UUID.randomUUID()).numeroCheque(r.numeroCheque()).montant(r.montant())
            .banqueEmettrice(r.banqueEmettrice()).dateEmission(r.dateEmission())
            .dateReception(r.dateReception() != null ? r.dateReception() : LocalDate.now())
            .statut(StatutCheque.RECU).facture(facture)
            .createdBy(loginAuteur).activeData(true).deletedData(false)
            .fromTable(TypeTable.CHEQUE_RECU_ATTESTATION).build();
        var saved = chequeRepository.save(cheque);
        var commande = facture.getCommande();
        if (commande != null && StatutCommandeAttestation.PROFORMA_EMISE.equals(commande.getStatut())) {
            commande.setStatut(StatutCommandeAttestation.CHEQUE_RECU);
            commande.setUpdatedBy(loginAuteur);
            commandeRepository.save(commande);
        }
        return DataResponse.created("Cheque " + r.numeroCheque() + " enregistre", mapper.toChequeResponse(saved));
    }

    @Override @Transactional(readOnly=true)
    public DataResponse<ChequeResponse> getCheque(UUID chequeId) {
        return DataResponse.success(mapper.toChequeResponse(chequeRepository.findActiveByTrackingId(chequeId)
            .orElseThrow(() -> new RessourceNotFoundException("Cheque introuvable"))));
    }

    @Override @Transactional(readOnly=true)
    public PaginatedResponse<ChequeResponse> getAllCheques(int page, int size) {
        return PaginatedResponse.fromPage(chequeRepository.findAllActive(PageRequest.of(page, size))
            .map(mapper::toChequeResponse), "Tous les cheques");
    }

    @Override @Transactional(readOnly=true)
    public DataResponse<List<ChequeResponse>> getChequesByCommande(UUID commandeId) {
        return DataResponse.success("Cheques de la commande",
            chequeRepository.findByCommande(commandeId).stream().map(mapper::toChequeResponse).toList());
    }

    @Override @Transactional
    public DataResponse<Void> encaisserCheque(UUID chequeId, LocalDate dateEncaissement, String loginAuteur) {
        var c = chequeRepository.findActiveByTrackingId(chequeId)
            .orElseThrow(() -> new RessourceNotFoundException("Cheque introuvable"));
        if (!StatutCheque.RECU.equals(c.getStatut()))
            throw new BadRequestException("Seul un cheque recu peut etre encaisse");
        c.setStatut(StatutCheque.ENCAISSE);
        c.setDateEncaissement(dateEncaissement != null ? dateEncaissement : LocalDate.now());
        c.setUpdatedBy(loginAuteur);
        chequeRepository.save(c);
        var commande = c.getFacture() != null ? c.getFacture().getCommande() : null;
        if (commande != null
            && (StatutCommandeAttestation.FACTURE_EMISE.equals(commande.getStatut())
                || StatutCommandeAttestation.LIVRE.equals(commande.getStatut()))) {
            commande.setStatut(StatutCommandeAttestation.SOLDE);
            commande.setUpdatedBy(loginAuteur);
            commandeRepository.save(commande);
        }
        return DataResponse.success("Cheque encaisse", null);
    }

    @Override @Transactional
    public DataResponse<Void> annulerCheque(UUID chequeId, String motif, String loginAuteur) {
        var c = chequeRepository.findActiveByTrackingId(chequeId)
            .orElseThrow(() -> new RessourceNotFoundException("Cheque introuvable"));
        c.setStatut(StatutCheque.ANNULE);
        c.setMotifAnnulation(motif);
        c.setUpdatedBy(loginAuteur);
        chequeRepository.save(c);
        return DataResponse.success("Cheque annule", null);
    }

    // ============== FACTURES ==============

    @Override @Transactional(readOnly=true)
    public DataResponse<FactureAttestationResponse> getFacture(UUID factureId) {
        return DataResponse.success(mapper.toFactureResponse(factureRepository.findActiveByTrackingId(factureId)
            .orElseThrow(() -> new RessourceNotFoundException("Facture introuvable"))));
    }

    @Override @Transactional(readOnly=true)
    public PaginatedResponse<FactureAttestationResponse> getAllFactures(int page, int size) {
        return PaginatedResponse.fromPage(factureRepository.findAllActive(PageRequest.of(page, size))
            .map(mapper::toFactureResponse), "Toutes les factures");
    }

    @Override @Transactional(readOnly=true)
    public DataResponse<List<FactureAttestationResponse>> getFacturesByCommande(UUID commandeId) {
        return DataResponse.success("Factures de la commande",
            factureRepository.findByCommande(commandeId).stream().map(mapper::toFactureResponse).toList());
    }

    // ============== TRANCHES DE LIVRAISON ==============

    @Override @Transactional
    public DataResponse<TrancheLivraisonResponse> livrerTranche(UUID commandeId, TrancheLivraisonRequest r, String loginAuteur) {
        var commande = commandeRepository.findActiveByTrackingId(commandeId)
            .orElseThrow(() -> new RessourceNotFoundException("Commande introuvable"));
        if (StatutCommandeAttestation.ANNULEE.equals(commande.getStatut()))
            throw new BadRequestException("Impossible de livrer une commande annulee");
        var lot = lotRepository.findActiveByTrackingId(r.lotTrackingId())
            .orElseThrow(() -> new RessourceNotFoundException("Lot introuvable"));

        long dejaLivreCommande = trancheRepository.sommeLivreParCommande(commandeId);
        long restantCommande = commande.getQuantite() - dejaLivreCommande;
        if (r.quantiteLivree() > restantCommande)
            throw new BadRequestException("Quantite a livrer (" + r.quantiteLivree() + ") superieure au reste a livrer (" + restantCommande + ")");

        long dejaLivreLot = trancheRepository.sommeLivreParLot(r.lotTrackingId());
        long restantLot = lot.getQuantite() - dejaLivreLot;
        if (r.quantiteLivree() > restantLot)
            throw new BadRequestException("Quantite a livrer superieure au stock restant du lot (" + restantLot + ")");

        TrancheLivraisonAttestation t = TrancheLivraisonAttestation.builder()
            .trancheTrackingId(UUID.randomUUID()).numeroDebutSerie(r.numeroDebutSerie())
            .numeroFinSerie(r.numeroFinSerie()).quantiteLivree(r.quantiteLivree())
            .dateLivraison(r.dateLivraison()).commande(commande).lot(lot)
            .createdBy(loginAuteur).activeData(true).deletedData(false)
            .fromTable(TypeTable.TRANCHE_LIVRAISON_ATTESTATION).build();
        var saved = trancheRepository.save(t);

        // Lot de la commande (pour traçabilité, premier lot utilisé)
        if (commande.getLot() == null) commande.setLot(lot);

        // Si totalement livré → statut LIVRE + date livraison effective
        if (dejaLivreCommande + r.quantiteLivree() >= commande.getQuantite()) {
            commande.setStatut(StatutCommandeAttestation.LIVRE);
            commande.setDateLivraisonEffective(r.dateLivraison());
        }
        commande.setUpdatedBy(loginAuteur);
        commandeRepository.save(commande);

        // Si lot épuisé → DISTRIBUE
        if (dejaLivreLot + r.quantiteLivree() >= lot.getQuantite()) {
            lot.setStatutLot(StatutLot.DISTRIBUE);
            lot.setUpdatedBy(loginAuteur);
            lotRepository.save(lot);
        }
        return DataResponse.created("Tranche livree", mapper.toTrancheResponse(saved));
    }

    @Override @Transactional(readOnly=true)
    public DataResponse<List<TrancheLivraisonResponse>> getTranchesByCommande(UUID commandeId) {
        return DataResponse.success("Tranches de la commande",
            trancheRepository.findByCommande(commandeId).stream().map(mapper::toTrancheResponse).toList());
    }
}
