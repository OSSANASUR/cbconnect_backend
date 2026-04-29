package com.ossanasur.cbconnect.module.finance.mapper;

import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.module.auth.repository.UtilisateurRepository;
import com.ossanasur.cbconnect.module.comptabilite.entity.EcritureComptable;
import com.ossanasur.cbconnect.module.finance.dto.response.PrefinancementDetailResponse;
import com.ossanasur.cbconnect.module.finance.dto.response.PrefinancementDetailResponse.EcritureInfo;
import com.ossanasur.cbconnect.module.finance.dto.response.PrefinancementDetailResponse.RemboursementInfo;
import com.ossanasur.cbconnect.module.finance.dto.response.PrefinancementResponse;
import com.ossanasur.cbconnect.module.finance.entity.Prefinancement;
import com.ossanasur.cbconnect.module.finance.entity.PrefinancementRemboursement;
import com.ossanasur.cbconnect.module.finance.repository.PrefinancementRemboursementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PrefinancementMapper {

    private final PrefinancementRemboursementRepository remboursementRepository;
    private final UtilisateurRepository utilisateurRepository;

    @NonNull
    public PrefinancementResponse toResponse(@NonNull Prefinancement p) {
        BigDecimal rembourse = remboursementRepository.sumMontantByPrefinancement(p.getHistoriqueId());
        BigDecimal reste = p.getMontantPrefinance().subtract(rembourse);
        return new PrefinancementResponse(
                p.getPrefinancementTrackingId(),
                p.getNumeroPrefinancement(),
                p.getSinistre() != null ? p.getSinistre().getSinistreTrackingId() : null,
                p.getSinistre() != null ? p.getSinistre().getNumeroSinistreLocal() : null,
                p.getMontantPrefinance(),
                rembourse,
                reste,
                p.getDatePrefinancement(),
                p.getDateValidation(),
                p.getStatut(),
                p.getCreatedAt(),
                formatLogin(p.getCreatedBy()));
    }

    @NonNull
    public PrefinancementDetailResponse toDetailResponse(@NonNull Prefinancement p) {
        BigDecimal rembourse = remboursementRepository.sumMontantByPrefinancement(p.getHistoriqueId());
        BigDecimal reste = p.getMontantPrefinance().subtract(rembourse);
        List<PrefinancementRemboursement> remboursements = remboursementRepository
                .findByPrefinancement(p.getPrefinancementTrackingId());
        return new PrefinancementDetailResponse(
                p.getPrefinancementTrackingId(),
                p.getNumeroPrefinancement(),
                p.getSinistre() != null ? p.getSinistre().getSinistreTrackingId() : null,
                p.getSinistre() != null ? p.getSinistre().getNumeroSinistreLocal() : null,
                p.getMontantPrefinance(),
                rembourse,
                reste,
                p.getDatePrefinancement(),
                p.getDateValidation(),
                p.getStatut(),
                p.getCreatedAt(),
                formatLogin(p.getCreatedBy()),
                p.getMotifDemande(),
                p.getMotifAnnulation(),
                loginOf(p.getValidePar()),
                loginOf(p.getAnnulePar()),
                toEcritureInfo(p.getEcritureComptable()),
                toRemboursementInfoList(remboursements));
    }

    @NonNull
    private List<RemboursementInfo> toRemboursementInfoList(@Nullable List<PrefinancementRemboursement> list) {
        if (list == null || list.isEmpty()) return Collections.emptyList();
        return list.stream().map(this::toRemboursementInfo).toList();
    }

    @NonNull
    private RemboursementInfo toRemboursementInfo(@NonNull PrefinancementRemboursement r) {
        return new RemboursementInfo(
                r.getRemboursementTrackingId(),
                r.getEncaissementSource() != null ? r.getEncaissementSource().getEncaissementTrackingId() : null,
                r.getEncaissementSource() != null ? r.getEncaissementSource().getNumeroCheque() : null,
                r.getMontant(),
                r.getDateRemboursement(),
                loginOf(r.getValidePar()),
                toEcritureInfo(r.getEcritureComptable()));
    }

    @Nullable
    private EcritureInfo toEcritureInfo(@Nullable EcritureComptable e) {
        if (e == null) return null;
        return new EcritureInfo(
                e.getEcritureTrackingId(),
                e.getNumeroEcriture(),
                e.getStatut() != null ? e.getStatut().name() : null,
                e.getMontantTotal(),
                e.getDateEcriture());
    }

    /** Formatte un Utilisateur entité en "Nom Prénoms (email)" — fallback email/username si nom manque. */
    @Nullable
    private String loginOf(@Nullable Utilisateur u) {
        if (u == null) return null;
        return formatUserDisplay(u);
    }

    /** Formatte une chaîne login (= email ou username) en "Nom Prénoms (email)". */
    @Nullable
    private String formatLogin(@Nullable String login) {
        if (login == null || login.isBlank()) return null;
        return utilisateurRepository.findByEmailOrUsername(login, login)
                .map(this::formatUserDisplay)
                .orElse(login);
    }

    private String formatUserDisplay(@NonNull Utilisateur u) {
        String nom = u.getNom() != null ? u.getNom().trim() : "";
        String prenoms = u.getPrenoms() != null ? u.getPrenoms().trim() : "";
        String email = u.getEmail() != null ? u.getEmail() : (u.getUsername() != null ? u.getUsername() : "");
        String fullName = (nom + " " + prenoms).trim();
        if (fullName.isEmpty()) return email;
        if (email.isEmpty()) return fullName;
        return fullName + " (" + email + ")";
    }
}
