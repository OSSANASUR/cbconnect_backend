package com.ossanasur.cbconnect.historique;

import com.ossanasur.cbconnect.module.auth.repository.OrganismeRepository;
import com.ossanasur.cbconnect.module.auth.repository.UtilisateurRepository;
import com.ossanasur.cbconnect.module.pays.repository.PaysRepository;
import com.ossanasur.cbconnect.module.auth.entity.Organisme;
import com.ossanasur.cbconnect.module.sinistre.dto.request.SinistreRequest;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import com.ossanasur.cbconnect.module.sinistre.repository.AssureRepository;
import com.ossanasur.cbconnect.module.sinistre.repository.SinistreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SinistreVersioningService extends AbstractVersioningService<Sinistre, SinistreRequest> {
    private final SinistreRepository repository;
    private final PaysRepository paysRepository;
    private final OrganismeRepository organismeRepository;
    @SuppressWarnings("unused")
    private final AssureRepository assureRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Override
    protected JpaRepository<Sinistre, Integer> getRepository() {
        return repository;
    }

    @Override
    protected Sinistre findActiveByTrackingId(UUID id) {
        return repository.findActiveByTrackingId(id).orElse(null);
    }

    @Override
    protected UUID getTrackingId(Sinistre e) {
        return e.getSinistreTrackingId();
    }

    @Override
    protected Sinistre mapToEntity(SinistreRequest r, Sinistre ex) {
        Sinistre u = cloneEntity(ex);

        // Identification & contexte
        if (r.typeSinistre() != null) u.setTypeSinistre(r.typeSinistre());
        if (r.typeDommage() != null)  u.setTypeDommage(r.typeDommage());
        if (r.numeroSinistreManuel() != null)      u.setNumeroSinistreManuel(r.numeroSinistreManuel());
        if (r.numeroSinistreHomologue() != null)   u.setNumeroSinistreHomologue(r.numeroSinistreHomologue());
        if (r.numeroSinistreEcarteBrune() != null) u.setNumeroSinistreEcarteBrune(r.numeroSinistreEcarteBrune());

        // Accident
        if (r.dateAccident() != null)    u.setDateAccident(r.dateAccident());
        if (r.dateDeclaration() != null) u.setDateDeclaration(r.dateDeclaration());
        if (r.lieuAccident() != null)    u.setLieuAccident(r.lieuAccident());
        u.setAgglomeration(r.agglomeration());
        if (r.positionRc() != null)      u.setPositionRc(r.positionRc());
        if (r.heureAccident() != null)   u.setHeureAccident(r.heureAccident());
        if (r.ville() != null)           u.setVille(r.ville());
        if (r.commune() != null)         u.setCommune(r.commune());
        if (r.provenance() != null)      u.setProvenance(r.provenance());
        if (r.destination() != null)     u.setDestination(r.destination());
        if (r.circonstances() != null)   u.setCirconstances(r.circonstances());
        if (r.pvEtabli() != null)        u.setPvEtabli(r.pvEtabli());

        // Bureaux / organismes
        if (r.paysEmetteurTrackingId() != null)
            paysRepository.findActiveByTrackingId(r.paysEmetteurTrackingId()).ifPresent(u::setPaysEmetteur);
        if (r.paysGestionnaireTrackingId() != null)
            paysRepository.findActiveByTrackingId(r.paysGestionnaireTrackingId()).ifPresent(u::setPaysGestionnaire);
        if (r.organismeMembreTrackingId() != null)
            organismeRepository.findActiveByTrackingId(r.organismeMembreTrackingId()).ifPresent(u::setOrganismeMembre);
        if (r.organismeHomologueTrackingId() != null)
            organismeRepository.findActiveByTrackingId(r.organismeHomologueTrackingId()).ifPresent(u::setOrganismeHomologue);
        if (r.redacteurTrackingId() != null)
            utilisateurRepository
                    .findByUtilisateurTrackingIdAndActiveDataTrueAndDeletedDataFalse(r.redacteurTrackingId())
                    .ifPresent(u::setRedacteur);

        // Contrat
        if (r.dateEffet() != null)    u.setDateEffet(r.dateEffet());
        if (r.dateEcheance() != null) u.setDateEcheance(r.dateEcheance());

        // Assureurs secondaires (remplace intégralement la liste)
        if (r.assureursSecondairesTrackingIds() != null) {
            Set<Organisme> secs = new HashSet<>();
            for (UUID oid : r.assureursSecondairesTrackingIds()) {
                organismeRepository.findActiveByTrackingId(oid).ifPresent(secs::add);
            }
            u.setAssureursSecondaires(secs);
        }

        // Conducteur
        if (r.conducteurEstAssure() != null)  u.setConducteurEstAssure(r.conducteurEstAssure());
        if (r.conducteurNom() != null)        u.setConducteurNom(r.conducteurNom());
        if (r.conducteurPrenom() != null)     u.setConducteurPrenom(r.conducteurPrenom());
        if (r.conducteurDateNaissance() != null) u.setConducteurDateNaissance(r.conducteurDateNaissance());
        if (r.conducteurNumeroPermis() != null)  u.setConducteurNumeroPermis(r.conducteurNumeroPermis());
        if (r.conducteurCategoriesPermis() != null)
            u.setConducteurCategoriesPermis(String.join(",", r.conducteurCategoriesPermis()));
        if (r.conducteurDateDelivrance() != null) u.setConducteurDateDelivrance(r.conducteurDateDelivrance());
        if (r.conducteurLieuDelivrance() != null) u.setConducteurLieuDelivrance(r.conducteurLieuDelivrance());

        // Déclarant
        if (r.declarantNom() != null)       u.setDeclarantNom(r.declarantNom());
        if (r.declarantPrenom() != null)    u.setDeclarantPrenom(r.declarantPrenom());
        if (r.declarantTelephone() != null) u.setDeclarantTelephone(r.declarantTelephone());
        if (r.declarantQualite() != null)   u.setDeclarantQualite(r.declarantQualite());

        return u;
    }
}
