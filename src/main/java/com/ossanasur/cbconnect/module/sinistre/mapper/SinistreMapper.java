package com.ossanasur.cbconnect.module.sinistre.mapper;

import com.ossanasur.cbconnect.module.auth.entity.Organisme;
import com.ossanasur.cbconnect.module.sinistre.dto.response.SinistreResponse;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SinistreMapper {
    public SinistreResponse toResponse(Sinistre s) {
        if (s == null) return null;

        List<SinistreResponse.AssureurSecondaireInfo> secondaires = List.of();
        Set<Organisme> set = s.getAssureursSecondaires();
        if (set != null && !set.isEmpty()) {
            secondaires = set.stream()
                .map(o -> new SinistreResponse.AssureurSecondaireInfo(
                        o.getOrganismeTrackingId(),
                        o.getRaisonSociale(),
                        o.getCode()))
                .collect(Collectors.toList());
        }

        return new SinistreResponse(
            s.getSinistreTrackingId(), s.getNumeroSinistreLocal(), s.getNumeroSinistreHomologue(),
            s.getTypeSinistre(), s.getStatut(), s.getTypeDommage(),
            s.getDateAccident(), s.getDateDeclaration(), s.getLieuAccident(), s.isAgglomeration(),
            s.getTauxRc(), s.getPositionRc(), s.isEstPrefinance(), s.isEstContentieux(),
            s.getPaysGestionnaire() != null ? s.getPaysGestionnaire().getPaysTrackingId() : null,
            s.getPaysGestionnaire() != null ? s.getPaysGestionnaire().getLibelle() : null,
            s.getPaysEmetteur() != null ? s.getPaysEmetteur().getPaysTrackingId() : null,
            s.getPaysEmetteur() != null ? s.getPaysEmetteur().getLibelle() : null,
            s.getOrganismeMembre() != null ? s.getOrganismeMembre().getOrganismeTrackingId() : null,
            s.getOrganismeMembre() != null ? s.getOrganismeMembre().getRaisonSociale() : null,
            s.getOrganismeMembre() != null ? s.getOrganismeMembre().getCode() : null,
            s.getOrganismeHomologue() != null ? s.getOrganismeHomologue().getOrganismeTrackingId() : null,
            s.getOrganismeHomologue() != null ? s.getOrganismeHomologue().getRaisonSociale() : null,
            s.getAssure() != null ? s.getAssure().getAssureTrackingId() : null,
            s.getAssure() != null ? s.getAssure().getNomComplet() : null,
            s.getAssure() != null ? s.getAssure().getImmatriculation() : null,
            s.getRedacteur() != null ? s.getRedacteur().getUtilisateurTrackingId() : null,
            s.getRedacteur() != null ? (s.getRedacteur().getNom() + " " + s.getRedacteur().getPrenoms()) : null,
            s.getCreatedAt(),

            /* V22 */
            s.getHeureAccident(),
            s.getVille(), s.getCommune(),
            s.getProvenance(), s.getDestination(),
            s.getCirconstances(),
            s.isPvEtabli(),
            s.getEntiteConstat() != null ? s.getEntiteConstat().getEntiteConstatTrackingId() : null,
            s.getEntiteConstat() != null ? s.getEntiteConstat().getNom() : null,
            s.getDateEffet(), s.getDateEcheance(),
            secondaires,
            s.isConducteurEstAssure(),
            s.getConducteurNom(), s.getConducteurPrenom(),
            s.getConducteurDateNaissance(),
            s.getConducteurNumeroPermis(),
            s.getConducteurCategoriesPermis(),
            s.getConducteurDateDelivrance(), s.getConducteurLieuDelivrance(),
            s.getDeclarantNom(), s.getDeclarantPrenom(),
            s.getDeclarantTelephone(), s.getDeclarantQualite(),
            /* V24 */
            s.getGarantieAcquise(),
            s.getReferenceGarantie(),
            s.getDateConfirmationGarantie(),
            s.getObservationsGarantie(),
            s.getCourrierNonGarantieRef(),
            s.getCourrierNonGarantieDate()
        );
    }
}
