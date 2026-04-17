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
}
