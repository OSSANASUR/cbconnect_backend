package com.ossanasur.cbconnect.module.auth.mapper;

import com.ossanasur.cbconnect.module.auth.dto.response.BanqueResponse;
import com.ossanasur.cbconnect.module.auth.entity.Banque;
import org.springframework.stereotype.Component;

@Component
public class BanqueMapper {
    public BanqueResponse toResponse(Banque b) {
        if (b == null) return null;
        return new BanqueResponse(
            b.getBanqueTrackingId(), b.getNom(), b.getCode(),
            b.getCodeBic(), b.getAgence(), b.getVille(),
            b.getCodePays(), b.getTelephone(), b.getCreatedAt()
        );
    }
}
