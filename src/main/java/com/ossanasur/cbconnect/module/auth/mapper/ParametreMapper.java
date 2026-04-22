package com.ossanasur.cbconnect.module.auth.mapper;
import com.ossanasur.cbconnect.module.auth.dto.response.ParametreResponse;
import com.ossanasur.cbconnect.module.auth.entity.Parametre;
import org.springframework.stereotype.Component;

@Component
public class ParametreMapper {
    public ParametreResponse toResponse(Parametre p) {
        if (p == null) return null;
        return new ParametreResponse(
            p.getParametreTrackingId(), p.getTypeParametre(),
            p.getCle(), p.getValeur(), p.getDescription()
        );
    }
}
