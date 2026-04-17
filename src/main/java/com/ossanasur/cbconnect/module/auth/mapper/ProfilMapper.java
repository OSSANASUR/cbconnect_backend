package com.ossanasur.cbconnect.module.auth.mapper;
import com.ossanasur.cbconnect.module.auth.dto.response.HabilitationResponse;
import com.ossanasur.cbconnect.module.auth.dto.response.ProfilResponse;
import com.ossanasur.cbconnect.module.auth.entity.Profil;
import com.ossanasur.cbconnect.security.entity.Habilitation;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProfilMapper {
    public ProfilResponse toResponse(Profil p) {
        if (p == null) return null;
        List<HabilitationResponse> habs = p.getHabilitations() == null ? Collections.emptyList() :
            p.getHabilitations().stream().map(this::toHabResponse).collect(Collectors.toList());
        return new ProfilResponse(
            p.getProfilTrackingId(), p.getProfilNom(), p.getCommentaire(),
            p.getOrganisme() != null ? p.getOrganisme().getOrganismeTrackingId() : null,
            p.getOrganisme() != null ? p.getOrganisme().getRaisonSociale() : null, habs
        );
    }
    private HabilitationResponse toHabResponse(Habilitation h) {
        return new HabilitationResponse(
            h.getHabilitationTrackingId(), h.getCodeHabilitation(), h.getLibelleHabilitation(),
            h.getDescription(), h.getAction(), h.getTypeAcces(),
            h.getModuleEntity() != null ? h.getModuleEntity().getNomModule() : null,
            h.getModuleEntity() != null ? h.getModuleEntity().getModuleTrackingId() : null
        );
    }
}
