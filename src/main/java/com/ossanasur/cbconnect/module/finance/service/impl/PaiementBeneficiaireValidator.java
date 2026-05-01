package com.ossanasur.cbconnect.module.finance.service.impl;

import com.ossanasur.cbconnect.common.enums.CategorieReglement;
import com.ossanasur.cbconnect.exception.BadRequestException;
import com.ossanasur.cbconnect.module.auth.entity.Organisme;
import com.ossanasur.cbconnect.module.expertise.entity.Expert;
import com.ossanasur.cbconnect.module.expertise.repository.AffectationExpertRepository;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import com.ossanasur.cbconnect.module.sinistre.entity.Victime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaiementBeneficiaireValidator {

    private final AffectationExpertRepository affectationExpertRepository;

    public void valider(CategorieReglement categorie,
            Sinistre sinistre,
            Victime victime,
            Organisme organisme,
            Expert expert) {

        if (categorie == null) {
            throw new BadRequestException("Le type de règlement est obligatoire");
        }

        long renseignes = (victime != null ? 1 : 0) + (organisme != null ? 1 : 0) + (expert != null ? 1 : 0);
        if (renseignes != 1) {
            throw new BadRequestException(
                    "Exactement un bénéficiaire doit être renseigné (Victime, Organisme ou Expert) — reçu : "
                            + renseignes);
        }

        switch (categorie) {
            case HONORAIRES -> {
                if (expert == null) {
                    throw new BadRequestException(
                            "Un règlement de catégorie HONORAIRES doit avoir un Expert comme bénéficiaire");
                }
                boolean affecte = affectationExpertRepository.existsActiveByExpertAndSinistre(
                        expert.getExpertTrackingId(), sinistre.getSinistreTrackingId());
                if (!affecte) {
                    throw new BadRequestException(
                            "L'expert sélectionné n'est pas affecté à ce sinistre");
                }
            }
            case PROVISIONS -> {
                if (victime == null) {
                    throw new BadRequestException(
                            "Un règlement de catégorie PROVISIONS doit avoir une Victime comme bénéficiaire");
                }
                if (victime.getSinistre() == null
                        || !sinistre.getSinistreTrackingId().equals(victime.getSinistre().getSinistreTrackingId())) {
                    throw new BadRequestException(
                            "La victime sélectionnée n'appartient pas à ce sinistre");
                }
            }
            case PRINCIPAL, FRAIS_ACCESSOIRES -> {
                if (victime != null
                        && (victime.getSinistre() == null
                                || !sinistre.getSinistreTrackingId()
                                        .equals(victime.getSinistre().getSinistreTrackingId()))) {
                    throw new BadRequestException(
                            "La victime sélectionnée n'appartient pas à ce sinistre");
                }
            }
        }
    }
}
