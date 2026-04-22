package com.ossanasur.cbconnect.historique;

import com.ossanasur.cbconnect.module.auth.dto.request.OrganismeRequest;
import com.ossanasur.cbconnect.module.auth.entity.Organisme;
import com.ossanasur.cbconnect.module.auth.repository.OrganismeRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganismeVersioningService extends AbstractVersioningService<Organisme, OrganismeRequest> {
    private final OrganismeRepository repository;

    @Override
    protected JpaRepository<Organisme, Integer> getRepository() {
        return repository;
    }

    @Override
    protected Organisme findActiveByTrackingId(UUID id) {
        return repository.findActiveByTrackingId(id).orElse(null);
    }

    @Override
    protected UUID getTrackingId(Organisme e) {
        return e.getOrganismeTrackingId();
    }

    @Override
    protected Organisme mapToEntity(OrganismeRequest r, Organisme existing) {
        Organisme u = cloneEntity(existing);
        if (r.typeOrganisme() != null) u.setTypeOrganisme(r.typeOrganisme());
        if (r.raisonSociale() != null) u.setRaisonSociale(r.raisonSociale());
        if (r.code() != null) u.setCode(r.code());
        if (r.email() != null) u.setEmail(r.email());
        if (r.responsable() != null) u.setResponsable(r.responsable());
        if (r.contacts() != null) u.setContacts(r.contacts());
        if (r.codePays() != null) u.setCodePays(r.codePays());
        if (r.codePaysBCB() != null) u.setCodePaysBCB(r.codePaysBCB());
        if (r.paysId() != null) u.setPaysId(r.paysId());
        if (r.dateCreation() != null) u.setDateCreation(r.dateCreation());
        if (r.numeroAgrement() != null) u.setNumeroAgrement(r.numeroAgrement());
        if (r.apiEndpointUrl() != null) u.setApiEndpointUrl(r.apiEndpointUrl());
        if (r.adresse() != null) u.setAdresse(r.adresse());
        if (r.boitePostale() != null) u.setBoitePostale(r.boitePostale());
        if (r.ville() != null) u.setVille(r.ville());
        if (r.telephonePrincipal() != null) u.setTelephonePrincipal(r.telephonePrincipal());
        if (r.fax() != null) u.setFax(r.fax());
        if (r.siteWeb() != null) u.setSiteWeb(r.siteWeb());
        u.setActive(r.active());
        return u;
    }

    @Override
    protected void setTrackingId(Organisme entity, UUID newId) {
        entity.setOrganismeTrackingId(newId);
    }
}
