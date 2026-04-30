package com.ossanasur.cbconnect.module.courrier.service.impl;

import com.ossanasur.cbconnect.common.enums.StatutBordereau;
import com.ossanasur.cbconnect.common.enums.TypeTable;
import com.ossanasur.cbconnect.exception.BadRequestException;
import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.module.auth.entity.Organisme;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.module.auth.repository.OrganismeRepository;
import com.ossanasur.cbconnect.module.auth.repository.UtilisateurRepository;
import com.ossanasur.cbconnect.module.courrier.dto.request.BordereauCoursierRequest;
import com.ossanasur.cbconnect.module.courrier.dto.request.ConfirmerDechargeRequest;
import com.ossanasur.cbconnect.module.courrier.dto.request.MarquerRemisTransporteurRequest;
import com.ossanasur.cbconnect.module.courrier.dto.response.BordereauCoursierResponse;
import com.ossanasur.cbconnect.module.courrier.dto.response.CourrierResponse;
import com.ossanasur.cbconnect.module.courrier.entity.BordereauCoursier;
import com.ossanasur.cbconnect.module.courrier.entity.Courrier;
import com.ossanasur.cbconnect.module.courrier.mapper.BordereauCoursierMapper;
import com.ossanasur.cbconnect.module.courrier.mapper.CourrierMapper;
import com.ossanasur.cbconnect.module.courrier.repository.BordereauCoursierRepository;
import com.ossanasur.cbconnect.module.courrier.repository.CourrierRepository;
import com.ossanasur.cbconnect.module.courrier.service.BordereauCoursierService;
import com.ossanasur.cbconnect.utils.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BordereauCoursierServiceImpl implements BordereauCoursierService {

    private final BordereauCoursierRepository bordereauRepository;
    private final CourrierRepository courrierRepository;
    private final OrganismeRepository organismeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final BordereauCoursierMapper mapper;
    private final CourrierMapper courrierMapper;

    // ═══════════════════════════════════════════════════════════════════════
    // Création / mise à jour
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public DataResponse<BordereauCoursierResponse> creer(BordereauCoursierRequest r, String loginAuteur) {
        validerDestinataire(r);

        Organisme dest = r.destinataireOrganismeTrackingId() != null
            ? organismeRepository.findActiveByTrackingId(r.destinataireOrganismeTrackingId())
                .orElseThrow(() -> new RessourceNotFoundException("Bureau homologue introuvable"))
            : null;

        Utilisateur coursier = r.coursierUtilisateurTrackingId() != null
            ? utilisateurRepository
                .findByUtilisateurTrackingIdAndActiveDataTrueAndDeletedDataFalse(r.coursierUtilisateurTrackingId())
                .orElseThrow(() -> new RessourceNotFoundException("Coursier introuvable"))
            : null;

        BordereauCoursier b = BordereauCoursier.builder()
            .bordereauTrackingId(UUID.randomUUID())
            .numeroBordereau(genererNumero())
            .destinataireOrganisme(dest)
            .destinataireLibre(r.destinataireLibre())
            .lieuDepart(r.lieuDepart() != null ? r.lieuDepart() : "Lomé")
            .dateCreation(LocalDateTime.now())
            .coursier(coursier)
            .transporteur(r.transporteur())
            .nomCompagnieBus(r.nomCompagnieBus())
            .referenceTransporteur(r.referenceTransporteur())
            .montantTransporteur(r.montantTransporteur())
            .statut(StatutBordereau.BROUILLON)
            .observations(r.observations())
            .createdBy(loginAuteur).activeData(true).deletedData(false)
            .fromTable(TypeTable.BORDEREAU_COURSIER)
            .build();

        b = bordereauRepository.save(b);

        // Attacher les courriers demandés — dans l'ordre fourni
        if (r.courriersTrackingIds() != null && !r.courriersTrackingIds().isEmpty()) {
            int ordre = 1;
            for (UUID cid : r.courriersTrackingIds()) {
                Courrier c = courrierRepository.findActiveByTrackingId(cid)
                    .orElseThrow(() -> new RessourceNotFoundException("Courrier introuvable : " + cid));
                if (c.getBordereau() != null)
                    throw new BadRequestException("Le courrier " + c.getReferenceCourrier()
                        + " est déjà sur un bordereau");
                c.setBordereau(b);
                c.setOrdreDansBordereau(ordre++);
                if (dest != null && c.getDestinataireOrganisme() == null) c.setDestinataireOrganisme(dest);
                courrierRepository.save(c);
            }
            b.getCourriers().clear();
            b.getCourriers().addAll(courrierRepository.findByBordereau(b.getBordereauTrackingId()));
        }

        return DataResponse.created("Bordereau créé : " + b.getNumeroBordereau(), mapper.toResponse(b));
    }

    @Override
    @Transactional
    public DataResponse<BordereauCoursierResponse> modifier(UUID id, BordereauCoursierRequest r, String loginAuteur) {
        BordereauCoursier b = getActif(id);
        if (b.getStatut() != StatutBordereau.BROUILLON)
            throw new BadRequestException("Un bordereau " + b.getStatut() + " n'est plus modifiable");

        validerDestinataire(r);

        if (r.destinataireOrganismeTrackingId() != null) {
            b.setDestinataireOrganisme(organismeRepository
                .findActiveByTrackingId(r.destinataireOrganismeTrackingId())
                .orElseThrow(() -> new RessourceNotFoundException("Bureau homologue introuvable")));
            b.setDestinataireLibre(null);
        } else if (r.destinataireLibre() != null) {
            b.setDestinataireLibre(r.destinataireLibre());
            b.setDestinataireOrganisme(null);
        }

        if (r.lieuDepart() != null) b.setLieuDepart(r.lieuDepart());
        if (r.coursierUtilisateurTrackingId() != null) {
            b.setCoursier(utilisateurRepository
                .findByUtilisateurTrackingIdAndActiveDataTrueAndDeletedDataFalse(r.coursierUtilisateurTrackingId())
                .orElseThrow(() -> new RessourceNotFoundException("Coursier introuvable")));
        }
        if (r.transporteur() != null) b.setTransporteur(r.transporteur());
        b.setNomCompagnieBus(r.nomCompagnieBus());
        if (r.referenceTransporteur() != null) b.setReferenceTransporteur(r.referenceTransporteur());
        if (r.montantTransporteur() != null) b.setMontantTransporteur(r.montantTransporteur());
        if (r.observations() != null) b.setObservations(r.observations());
        b.setUpdatedBy(loginAuteur);

        return DataResponse.success("Bordereau modifié", mapper.toResponse(bordereauRepository.save(b)));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Lecture
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public DataResponse<BordereauCoursierResponse> getByTrackingId(UUID id) {
        return DataResponse.success(mapper.toResponse(getActif(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<BordereauCoursierResponse>> getAll() {
        return DataResponse.success(
            bordereauRepository.findAllActive().stream().map(mapper::toResponse).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<BordereauCoursierResponse>> getByStatut(StatutBordereau statut) {
        return DataResponse.success(
            bordereauRepository.findByStatut(statut).stream().map(mapper::toResponse).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<CourrierResponse>> getCourriersPretsAEmbarquer(UUID destinataireOrganismeTrackingId) {
        List<Courrier> list = destinataireOrganismeTrackingId != null
            ? courrierRepository.findSortantsPretsPourDestinataire(destinataireOrganismeTrackingId)
            : courrierRepository.findSortantsPretsAEmbarquer();
        return DataResponse.success(list.stream().map(courrierMapper::toResponse).toList());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Machine d'état (étapes 1 → 4)
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public DataResponse<BordereauCoursierResponse> marquerImprime(UUID id, String loginAuteur) {
        BordereauCoursier b = getActif(id);
        if (b.getStatut() != StatutBordereau.BROUILLON)
            throw new BadRequestException("Seul un BROUILLON peut être imprimé");
        if (b.getCourriers() == null || b.getCourriers().isEmpty())
            throw new BadRequestException("Le bordereau ne contient aucun courrier");

        b.setStatut(StatutBordereau.IMPRIME);
        b.setDateRemiseCoursier(LocalDateTime.now());
        b.setUpdatedBy(loginAuteur);
        return DataResponse.success("Bordereau imprimé — remis au coursier",
            mapper.toResponse(bordereauRepository.save(b)));
    }

    @Override
    @Transactional
    public DataResponse<BordereauCoursierResponse> marquerRemisTransporteur(
            UUID id, MarquerRemisTransporteurRequest r, String loginAuteur) {
        BordereauCoursier b = getActif(id);
        if (b.getStatut() != StatutBordereau.IMPRIME)
            throw new BadRequestException("Le bordereau doit être IMPRIME pour être remis à un transporteur");

        b.setReferenceTransporteur(r.referenceTransporteur());
        if (r.montantTransporteur() != null) b.setMontantTransporteur(r.montantTransporteur());
        if (r.nomCompagnieBus() != null)    b.setNomCompagnieBus(r.nomCompagnieBus());
        if (r.factureGedDocumentId() != null) b.setFactureGedDocumentId(r.factureGedDocumentId());
        if (r.observations() != null)       b.setObservations(r.observations());

        b.setStatut(StatutBordereau.REMIS_TRANSPORTEUR);
        b.setDateRemiseTransporteur(LocalDateTime.now());
        b.setUpdatedBy(loginAuteur);
        return DataResponse.success("Bordereau remis au transporteur",
            mapper.toResponse(bordereauRepository.save(b)));
    }

    @Override
    @Transactional
    public DataResponse<BordereauCoursierResponse> confirmerDechargeRecue(
            UUID id, ConfirmerDechargeRequest r, String loginAuteur) {
        BordereauCoursier b = getActif(id);
        if (b.getStatut() != StatutBordereau.REMIS_TRANSPORTEUR)
            throw new BadRequestException("La décharge ne peut être confirmée que pour un bordereau REMIS_TRANSPORTEUR");

        b.setDechargeGedDocumentId(r.dechargeGedDocumentId());
        if (r.observations() != null) b.setObservations(r.observations());

        b.setStatut(StatutBordereau.DECHARGE_RECUE);
        b.setDateDechargeRecue(LocalDateTime.now());
        b.setUpdatedBy(loginAuteur);
        return DataResponse.success("Décharge reçue et archivée",
            mapper.toResponse(bordereauRepository.save(b)));
    }

    @Override
    @Transactional
    public DataResponse<BordereauCoursierResponse> marquerRetourne(UUID id, String motif, String loginAuteur) {
        BordereauCoursier b = getActif(id);
        if (b.getStatut() == StatutBordereau.DECHARGE_RECUE)
            throw new BadRequestException("Un bordereau avec décharge reçue ne peut pas être marqué retourné");

        b.setStatut(StatutBordereau.RETOURNE);
        b.setObservations((b.getObservations() != null ? b.getObservations() + "\n" : "")
            + "[RETOURNE] " + (motif != null ? motif : ""));
        b.setUpdatedBy(loginAuteur);

        // Les courriers du bordereau redeviennent embarquables
        if (b.getCourriers() != null) {
            for (Courrier c : b.getCourriers()) {
                c.setBordereau(null);
                c.setOrdreDansBordereau(null);
                courrierRepository.save(c);
            }
        }
        return DataResponse.success("Bordereau marqué retourné",
            mapper.toResponse(bordereauRepository.save(b)));
    }

    @Override
    @Transactional
    public DataResponse<Void> supprimer(UUID id, String loginAuteur) {
        BordereauCoursier b = getActif(id);
        if (b.getStatut() != StatutBordereau.BROUILLON)
            throw new BadRequestException("Seul un BROUILLON peut être supprimé");

        // Détacher les courriers avant soft-delete
        if (b.getCourriers() != null) {
            for (Courrier c : b.getCourriers()) {
                c.setBordereau(null);
                c.setOrdreDansBordereau(null);
                courrierRepository.save(c);
            }
        }
        b.setActiveData(false);
        b.setDeletedData(true);
        b.setDeletedAt(LocalDateTime.now());
        b.setDeletedBy(loginAuteur);
        bordereauRepository.save(b);
        return DataResponse.success("Bordereau supprimé", null);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Gestion des lignes (courriers embarqués)
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public DataResponse<BordereauCoursierResponse> ajouterCourrier(
            UUID bordereauId, UUID courrierId, Integer ordre, String loginAuteur) {
        BordereauCoursier b = getActif(bordereauId);
        if (b.getStatut() != StatutBordereau.BROUILLON)
            throw new BadRequestException("Seul un BROUILLON accepte de nouveaux courriers");

        Courrier c = courrierRepository.findActiveByTrackingId(courrierId)
            .orElseThrow(() -> new RessourceNotFoundException("Courrier introuvable"));
        if (c.getBordereau() != null)
            throw new BadRequestException("Ce courrier est déjà sur un autre bordereau");

        int ordreFinal = ordre != null ? ordre
            : ((b.getCourriers() == null ? 0 : b.getCourriers().size()) + 1);

        c.setBordereau(b);
        c.setOrdreDansBordereau(ordreFinal);
        if (b.getDestinataireOrganisme() != null && c.getDestinataireOrganisme() == null)
            c.setDestinataireOrganisme(b.getDestinataireOrganisme());
        courrierRepository.save(c);

        b.setUpdatedBy(loginAuteur);
        bordereauRepository.save(b);
        return DataResponse.success("Courrier ajouté au bordereau",
            mapper.toResponse(getActif(bordereauId)));
    }

    @Override
    @Transactional
    public DataResponse<BordereauCoursierResponse> retirerCourrier(
            UUID bordereauId, UUID courrierId, String loginAuteur) {
        BordereauCoursier b = getActif(bordereauId);
        if (b.getStatut() != StatutBordereau.BROUILLON)
            throw new BadRequestException("Un bordereau " + b.getStatut() + " n'est plus modifiable");

        Courrier c = courrierRepository.findActiveByTrackingId(courrierId)
            .orElseThrow(() -> new RessourceNotFoundException("Courrier introuvable"));
        if (c.getBordereau() == null || !c.getBordereau().getBordereauTrackingId().equals(bordereauId))
            throw new BadRequestException("Ce courrier n'appartient pas au bordereau");

        c.setBordereau(null);
        c.setOrdreDansBordereau(null);
        courrierRepository.save(c);

        b.setUpdatedBy(loginAuteur);
        bordereauRepository.save(b);
        return DataResponse.success("Courrier retiré du bordereau",
            mapper.toResponse(getActif(bordereauId)));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════════════════════════

    private BordereauCoursier getActif(UUID id) {
        return bordereauRepository.findActiveByTrackingId(id)
            .orElseThrow(() -> new RessourceNotFoundException("Bordereau introuvable"));
    }

    private void validerDestinataire(BordereauCoursierRequest r) {
        boolean orgPresent = r.destinataireOrganismeTrackingId() != null;
        boolean libPresent = r.destinataireLibre() != null && !r.destinataireLibre().isBlank();
        if (!orgPresent && !libPresent)
            throw new BadRequestException("Destinataire obligatoire : bureau homologue OU destinataire libre");
    }

    /** Génère BORD/AAAA/NNNN/BNCB-TG en prenant le prochain numéro séquentiel annuel. */
    private String genererNumero() {
        int annee = Year.now().getValue();
        long seq = bordereauRepository.countForYear(annee) + 1;
        return String.format("BORD/%d/%04d/BNCB-TG", annee, seq);
    }
}
