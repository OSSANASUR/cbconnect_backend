package com.ossanasur.cbconnect.module.sinistre.mapper;
import com.ossanasur.cbconnect.module.sinistre.dto.response.VictimeResponse;
import com.ossanasur.cbconnect.module.sinistre.entity.Victime;
import org.springframework.stereotype.Component;
@Component
public class VictimeMapper {
    public VictimeResponse toResponse(Victime v) {
        if (v==null) return null;
        return new VictimeResponse(
            v.getVictimeTrackingId(), v.getNom(), v.getPrenoms(), v.getDateNaissance(), v.getSexe(),
            v.getNationalite(), v.getTypeVictime(), v.getStatutVictime(), v.getStatutActivite(), v.getRevenuMensuel(),
            v.isEstDcdSuiteBlessures(), v.getDateDeces(), v.isLienDecesAccident(),
            v.getPaysResidence()!=null?v.getPaysResidence().getLibelle():null,
            v.getSinistre()!=null?v.getSinistre().getSinistreTrackingId():null
        );
    }
}
