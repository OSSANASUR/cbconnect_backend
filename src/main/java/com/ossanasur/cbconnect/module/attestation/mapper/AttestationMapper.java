package com.ossanasur.cbconnect.module.attestation.mapper;
import com.ossanasur.cbconnect.module.attestation.dto.response.*;
import com.ossanasur.cbconnect.module.attestation.entity.*;
import org.springframework.stereotype.Component;
@Component
public class AttestationMapper {
    public CommandeAttestationResponse toCommandeResponse(CommandeAttestation c) {
        if(c==null) return null;
        return new CommandeAttestationResponse(
            c.getCommandeTrackingId(), c.getNumeroCommande(), c.getStatut(), c.getQuantite(),
            c.getPrixUnitaireVente(), c.getMontantAttestation(), c.getMontantContributionFonds(),
            c.getMontantTotal(), c.getMontantEnLettres(), c.getDateCommande(), c.getDateLivraisonEffective(),
            c.getNomBeneficiaireCheque(), c.getOrganisme()!=null?c.getOrganisme().getRaisonSociale():null
        );
    }
    public FactureAttestationResponse toFactureResponse(FactureAttestation f) {
        if(f==null) return null;
        return new FactureAttestationResponse(
            f.getFactureTrackingId(), f.getNumeroFacture(), f.getTypeFacture(),
            f.getDateFacture(), f.getMontantAttestation(), f.getMontantContributionFonds(),
            f.getMontantTotal(), f.getMontantEnLettres(), f.getInstructionCheque(),
            f.getDateEcheance(), f.isAnnulee(),
            f.getCommande()!=null?f.getCommande().getNumeroCommande():null
        );
    }
    public LotResponse toLotResponse(LotApprovisionnement l, long quantiteDistribuee) {
        if(l==null) return null;
        int distribuee = (int) quantiteDistribuee;
        int restante = (l.getQuantite()!=null?l.getQuantite():0) - distribuee;
        return new LotResponse(
            l.getLotTrackingId(), l.getReferenceLot(), l.getNomFournisseur(), l.getNumeroBonCommande(),
            l.getQuantite(), l.getNumeroDebutSerie(), l.getNumeroFinSerie(), l.getPrixUnitaireAchat(),
            l.getDateCommande(), l.getDateLivraisonFournisseur(), l.getStatutLot(),
            distribuee, Math.max(0, restante)
        );
    }
    public ChequeResponse toChequeResponse(ChequeRecuAttestation c) {
        if(c==null) return null;
        var f = c.getFacture();
        var cmd = f!=null ? f.getCommande() : null;
        return new ChequeResponse(
            c.getChequeTrackingId(), c.getNumeroCheque(), c.getMontant(), c.getBanqueEmettrice(),
            c.getDateEmission(), c.getDateReception(), c.getDateEncaissement(),
            c.getStatut(), c.getMotifAnnulation(),
            f!=null ? f.getFactureTrackingId() : null,
            f!=null ? f.getNumeroFacture() : null,
            cmd!=null ? cmd.getCommandeTrackingId() : null,
            cmd!=null ? cmd.getNumeroCommande() : null,
            cmd!=null && cmd.getOrganisme()!=null ? cmd.getOrganisme().getRaisonSociale() : null
        );
    }
    public TrancheLivraisonResponse toTrancheResponse(TrancheLivraisonAttestation t) {
        if(t==null) return null;
        return new TrancheLivraisonResponse(
            t.getTrancheTrackingId(), t.getNumeroDebutSerie(), t.getNumeroFinSerie(),
            t.getQuantiteLivree(), t.getDateLivraison(),
            t.getCommande()!=null ? t.getCommande().getCommandeTrackingId() : null,
            t.getCommande()!=null ? t.getCommande().getNumeroCommande() : null,
            t.getLot()!=null ? t.getLot().getLotTrackingId() : null,
            t.getLot()!=null ? t.getLot().getReferenceLot() : null
        );
    }
}
