package com.ossanasur.cbconnect.module.baremes.mapper;

import com.ossanasur.cbconnect.module.baremes.dto.response.*;
import com.ossanasur.cbconnect.module.baremes.entity.*;
import org.springframework.stereotype.Component;

@Component
public class BaremesMapper {

    public BaremeCapitalisationResponse toResponse(BaremeCapitalisation b) {
        return new BaremeCapitalisationResponse(
                b.getId(), b.getTypeBareme(), b.getAge(),
                b.getPrixFrancRente(), b.getTauxCapitalisation(),
                b.getTableMortalite(), b.getAgeLimitePaiement(), b.isActif());
    }

    public BaremeValeurPointIpResponse toResponse(BaremeValeurPointIp b) {
        return new BaremeValeurPointIpResponse(
                b.getId(), b.getAgeMin(), b.getAgeMax(),
                b.getIppMin(), b.getIppMax(),
                b.getValeurPoint(), b.isActif());
    }

    public BaremeCleRepartition265Response toResponse(BaremeCleRepartition265 b) {
        return new BaremeCleRepartition265Response(
                b.getId(), b.getCodeSituation(), b.getLibelleSituation(),
                b.isConditionConjoint(), b.isConditionEnfant(), b.getNombreMaxEnfants(),
                b.getCleAscendants(), b.getCleConjoints(),
                b.getCleEnfants(), b.getCleOrphelinsDoubles(), b.isActif());
    }

    public BaremePrejudiceMoral266Response toResponse(BaremePrejudiceMoral266 b) {
        return new BaremePrejudiceMoral266Response(
                b.getId(), b.getLienParente(), b.getCle(),
                b.getPlafondCategorie(), b.isActif());
    }

    public BaremePretiumDolorisResponse toResponse(BaremePretiumDoloris b) {
        return new BaremePretiumDolorisResponse(
                b.getId(), b.getQualification(), b.getPoints(),
                b.isMoral(), b.isActif());
    }
}
