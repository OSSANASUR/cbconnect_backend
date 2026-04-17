package com.ossanasur.cbconnect.module.comptabilite.mapper;
import com.ossanasur.cbconnect.module.comptabilite.dto.response.*;
import com.ossanasur.cbconnect.module.comptabilite.entity.*;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;
@Component
public class ComptabiliteMapper {
    public EcritureResponse toResponse(EcritureComptable e) {
        if(e==null) return null;
        var lignes = e.getLignes()!=null ? e.getLignes().stream().map(l ->
            new LigneEcritureResponse(l.getCompte().getNumeroCompte(), l.getCompte().getLibelleCompte(),
                l.getSens(), l.getMontant(), l.getLibelleLigne())).collect(Collectors.toList()) : null;
        return new EcritureResponse(
            e.getEcritureTrackingId(), e.getNumeroEcriture(), e.getTypeTransaction(),
            e.getDateEcriture(), e.getLibelle(), e.getMontantTotal(), e.getStatut(),
            e.getJournal()!=null?e.getJournal().getCodeJournal():null,
            e.getSinistre()!=null?e.getSinistre().getNumeroSinistreLocal():null, lignes
        );
    }
}
