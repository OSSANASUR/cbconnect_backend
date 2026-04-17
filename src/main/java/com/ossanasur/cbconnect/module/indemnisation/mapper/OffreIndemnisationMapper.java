package com.ossanasur.cbconnect.module.indemnisation.mapper;
import com.ossanasur.cbconnect.module.indemnisation.dto.response.*;
import com.ossanasur.cbconnect.module.indemnisation.entity.*;
import org.springframework.stereotype.Component;
@Component
public class OffreIndemnisationMapper {
    public OffreIndemnisationResponse toResponse(OffreIndemnisation o) {
        if(o==null) return null;
        return new OffreIndemnisationResponse(
            o.getOffreTrackingId(), o.getSmigMensuelRetenu(),
            o.getMontantFraisMedicaux(), o.getMontantItt(),
            o.getMontantPrejPhysiologique(), o.getMontantPrejEconomique(),
            o.getMontantPrejMoral(), o.getMontantTiercePersonne(),
            o.getMontantPretiumDoloris(), o.getMontantPrejEsthetique(),
            o.getMontantPrejCarriere(), o.getMontantPrejScolaire(),
            o.getMontantPrejLeses(), o.getMontantFraisFuneraires(),
            o.getTotalBrut(), o.getTauxPartageRc(), o.getTotalNet(),
            o.getFraisGestion(), o.getMontantTotalOffre(),
            o.getDateValidation(),
            o.getVictime()!=null?o.getVictime().getNom()+" "+o.getVictime().getPrenoms():null,
            o.getValidePar()!=null?o.getValidePar().getNom()+" "+o.getValidePar().getPrenoms():null,
            o.getCreatedAt()
        );
    }
    public AyantDroitResponse toAyantDroitResponse(AyantDroit a) {
        if(a==null) return null;
        return new AyantDroitResponse(
            a.getAyantDroitTrackingId(), a.getNom(), a.getPrenoms(),
            a.getDateNaissance(), a.getSexe(), a.getLien(),
            a.isEstOrphelinDouble(), a.isPoursuiteEtudes(),
            a.getMontantPe(), a.getMontantPm(), a.getMontantTotal(),
            a.getVictime()!=null?a.getVictime().getNom()+" "+a.getVictime().getPrenoms():null
        );
    }
}
