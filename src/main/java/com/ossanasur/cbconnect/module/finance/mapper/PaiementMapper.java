package com.ossanasur.cbconnect.module.finance.mapper;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.ossanasur.cbconnect.common.enums.StatutPaiement;
import com.ossanasur.cbconnect.common.enums.TypeOperationFinanciere;
import com.ossanasur.cbconnect.common.enums.TypeTable;
import com.ossanasur.cbconnect.module.finance.dto.response.PaiementResponse;
import com.ossanasur.cbconnect.module.finance.dto.request.PaiementCreateRequest;
import com.ossanasur.cbconnect.module.auth.entity.Organisme;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.module.comptabilite.entity.EcritureComptable;
import com.ossanasur.cbconnect.module.finance.dto.response.PaiementDetailResponse;

import com.ossanasur.cbconnect.module.finance.dto.response.PaiementDetailResponse.BeneficiaireInfo;
import com.ossanasur.cbconnect.module.finance.dto.response.PaiementDetailResponse.EcritureInfo;
import com.ossanasur.cbconnect.module.finance.dto.response.PaiementDetailResponse.EncaissementLieInfo;
import com.ossanasur.cbconnect.module.finance.entity.Encaissement;
import com.ossanasur.cbconnect.module.finance.entity.Paiement;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import com.ossanasur.cbconnect.module.sinistre.entity.Victime;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
public class PaiementMapper {

    @NonNull
    public PaiementResponse toResponse(@NonNull Paiement p) {
        Sinistre s = p.getSinistre();
        return new PaiementResponse(
                p.getPaiementTrackingId(),
                p.getNumeroPaiement(),
                deriveType(p),
                p.getParentCodeId(),
                s != null ? s.getSinistreTrackingId() : null,
                s != null ? s.getLibelle() : null,
                p.getBeneficiaire(),
                p.getMontant(),
                p.getModePaiement(),
                p.getNumeroChequeEmis(),
                p.getBanqueCheque(),
                p.getDateEmission(),
                p.getDatePaiement(),
                p.getStatut(),
                p.isRepriseHistorique(),
                p.getCreatedAt(),
                p.getCreatedBy(),
                p.getUpdatedAt(),
                p.getUpdatedBy(),
                /* V2026042601 */
                p.getDateEmissionCheque(),
                p.getTypePrejudice(),
                p.getMotifComplement());
    }

    @NonNull
    public PaiementDetailResponse toDetailResponse(@NonNull Paiement p) {
        Sinistre s = p.getSinistre();
        return new PaiementDetailResponse(
                /* identifiants */
                p.getPaiementTrackingId(),
                p.getNumeroPaiement(),
                deriveType(p),
                s != null ? s.getSinistreTrackingId() : null,
                s != null ? s.getLibelle() : null,
                /* bénéficiaire */
                p.getBeneficiaire(),
                toBeneficiaireInfo(p),
                /* chèque */
                p.getMontant(),
                p.getModePaiement(),
                p.getNumeroChequeEmis(),
                p.getBanqueCheque(),
                /* dates */
                p.getDateEmission(),
                p.getDatePaiement(),
                /* statut */
                p.getStatut(),
                p.getMotifAnnulation(),
                loginOf(p.getAnnulePar()),
                /* relations */
                toEcritureInfo(p.getEcritureComptable()),
                toEncaissementInfoList(p.getEncaissements()),
                /* flags */
                p.isRepriseHistorique(),
                p.isExcel(),
                /* audit complet */
                p.getCreatedAt(),
                p.getCreatedBy(),
                p.getUpdatedAt(),
                p.getUpdatedBy(),
                p.getDeletedAt(),
                p.getDeletedBy(),
                p.getParentCodeId(),
                /* V2026042601 */
                p.getDateEmissionCheque(),
                p.getTypePrejudice(),
                p.getMotifComplement());
    }

    @NonNull
    public Paiement toNewEntity(
            @NonNull PaiementCreateRequest request,
            @NonNull Sinistre sinistre,
            @Nullable Victime victime,
            @Nullable Organisme organisme,
            @Nullable String createdBy) {

        validateBeneficiaireXOR(victime, organisme);

        return Paiement.builder()
                .paiementTrackingId(UUID.randomUUID())
                .sinistre(sinistre)
                /* bénéficiaire */
                .beneficiaire(request.beneficiaire())
                .beneficiaireVictime(victime)
                .beneficiaireOrganisme(organisme)
                /* chèque */
                // .numeroChequeEmis(request.numeroChequeEmis())
                // .banqueCheque(request.banqueCheque())
                // .modePaiement(request.modePaiement())
                /* financier */
                .montant(request.montant())
                .dateEmission(LocalDate.now())
                /* qualification du préjudice (V2026042601) */
                .typePrejudice(request.typePrejudice())
                .motifComplement(request.motifComplement())
                /* statut initial */
                .statut(StatutPaiement.EMIS)
                /* audit & historique */
                .createdBy(createdBy)
                .activeData(true)
                .deletedData(false)
                .fromTable(TypeTable.PAIEMENT)
                .build();
    }

    @Nullable
    private BeneficiaireInfo toBeneficiaireInfo(@NonNull Paiement p) {
        if (p.getBeneficiaireVictime() != null) {
            return toBeneficiaireVictime(p.getBeneficiaireVictime());
        }
        if (p.getBeneficiaireOrganisme() != null) {
            return toBeneficiaireOrganisme(p.getBeneficiaireOrganisme());
        }
        return null;
    }

    /**
     * Victime → {@link BeneficiaireInfo}.
     *
     * <p>
     * Le nom de la victime est lu via {@code Victime#getLibelle()} (champ
     * hérité de {@code InternalHistorique} qui stocke le libellé d'affichage).
     *
     * <p>
     * <strong>TODO :</strong> adapter si {@code Victime} expose un champ dédié
     * (ex. {@code nom + prenom}).
     */
    private BeneficiaireInfo toBeneficiaireVictime(@NonNull Victime v) {
        return BeneficiaireInfo.ofVictime(v.getVictimeTrackingId(), v.getLibelle());
    }

    /**
     * Organisme → {@link BeneficiaireInfo}.
     *
     * <p>
     * {@code typeOrganisme} est converti en texte via {@code name()}
     * pour éviter une dépendance de l'énumération dans la couche DTO.
     */
    private BeneficiaireInfo toBeneficiaireOrganisme(@NonNull Organisme o) {
        return BeneficiaireInfo.ofOrganisme(
                o.getOrganismeTrackingId(),
                o.getRaisonSociale(),
                o.getCode(),
                o.getTypeOrganisme() != null ? o.getTypeOrganisme().name() : null,
                o.getCodePaysBCB());
    }

    @Nullable
    private EcritureInfo toEcritureInfo(@Nullable EcritureComptable e) {
        if (e == null) {
            return null;
        }
        return new EcritureInfo(
                e.getEcritureTrackingId(),
                e.getNumeroEcriture(),
                e.getLibelle(),
                e.getStatut() != null ? e.getStatut().name() : null,
                e.getMontantTotal(),
                e.getDateEcriture());
    }

    @NonNull
    private List<EncaissementLieInfo> toEncaissementInfoList(
            @Nullable List<Encaissement> encaissements) {
        if (encaissements == null || encaissements.isEmpty()) {
            return Collections.emptyList();
        }
        return encaissements.stream()
                .filter(Objects::nonNull)
                .map(this::toEncaissementInfo)
                .toList();
    }

    @NonNull
    private EncaissementLieInfo toEncaissementInfo(@NonNull Encaissement enc) {
        return new EncaissementLieInfo(
                enc.getEncaissementTrackingId(),
                enc.getNumeroCheque(),
                enc.getMontantCheque(),
                enc.getDateEncaissement(),
                enc.getStatutCheque() != null ? enc.getStatutCheque().name() : null);
    }

    @Nullable
    private String loginOf(@Nullable Utilisateur u) {
        if (u == null) {
            return null;
        }
        return u.getUsername() != null ? u.getUsername() : u.getEmail();
    }

    private void validateBeneficiaireXOR(@Nullable Victime victime,
            @Nullable Organisme organisme) {
        boolean hasVictime = victime != null;
        boolean hasOrganisme = organisme != null;
        if (hasVictime == hasOrganisme) { // les deux true OU les deux false
            throw new IllegalArgumentException(
                    "Exactement un bénéficiaire doit être fourni : " +
                            "victime=" + hasVictime + ", organisme=" + hasOrganisme);
        }
    }

    /**
     * Dérive le type d'opération financière à partir du contexte d'une ligne Paiement.
     * Cohérent avec la convention de génération du numero_operation et le SQL
     * de backfill (cf. spec 2026-04-28, invariant § DTOs).
     */
    @NonNull
    private TypeOperationFinanciere deriveType(@NonNull Paiement p) {
        StatutPaiement statut = p.getStatut();
        if (statut == StatutPaiement.ANNULE) {
            return TypeOperationFinanciere.ANNULATION_REGLEMENT;
        }
        if (statut == StatutPaiement.REGLEMENT_COMPTABLE_VALIDE
                || statut == StatutPaiement.PAYE) {
            return TypeOperationFinanciere.REGLEMENT_COMPTABLE;
        }
        return TypeOperationFinanciere.REGLEMENT_TECHNIQUE;
    }
}
