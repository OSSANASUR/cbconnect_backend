package com.ossanasur.cbconnect.module.courrier.service;

import com.ossanasur.cbconnect.common.enums.StatutBordereau;
import com.ossanasur.cbconnect.module.courrier.dto.request.BordereauCoursierRequest;
import com.ossanasur.cbconnect.module.courrier.dto.request.ConfirmerDechargeRequest;
import com.ossanasur.cbconnect.module.courrier.dto.request.MarquerRemisTransporteurRequest;
import com.ossanasur.cbconnect.module.courrier.dto.response.BordereauCoursierResponse;
import com.ossanasur.cbconnect.module.courrier.dto.response.CourrierResponse;
import com.ossanasur.cbconnect.utils.DataResponse;

import java.util.List;
import java.util.UUID;

public interface BordereauCoursierService {

    DataResponse<BordereauCoursierResponse> creer(BordereauCoursierRequest r, String loginAuteur);

    DataResponse<BordereauCoursierResponse> modifier(UUID id, BordereauCoursierRequest r, String loginAuteur);

    DataResponse<BordereauCoursierResponse> getByTrackingId(UUID id);

    DataResponse<List<BordereauCoursierResponse>> getAll();

    DataResponse<List<BordereauCoursierResponse>> getByStatut(StatutBordereau statut);

    /** Marque le bordereau comme IMPRIME (figé, non modifiable). */
    DataResponse<BordereauCoursierResponse> marquerImprime(UUID id, String loginAuteur);

    /** Étape 2 : remis à la poste / bus / DHL. Saisie facture + référence. */
    DataResponse<BordereauCoursierResponse> marquerRemisTransporteur(
        UUID id, MarquerRemisTransporteurRequest r, String loginAuteur);

    /** Étape 3 : décharge signée reçue de l'homologue (scan archivé en GED). */
    DataResponse<BordereauCoursierResponse> confirmerDechargeRecue(
        UUID id, ConfirmerDechargeRequest r, String loginAuteur);

    /** Annulation : bordereau retourné sans remise. */
    DataResponse<BordereauCoursierResponse> marquerRetourne(UUID id, String motif, String loginAuteur);

    DataResponse<Void> supprimer(UUID id, String loginAuteur);

    /** Courriers SORTANTS PHYSIQUES non encore embarqués, éventuellement filtrés par destinataire. */
    DataResponse<List<CourrierResponse>> getCourriersPretsAEmbarquer(UUID destinataireOrganismeTrackingId);

    /** Ajouter un courrier à un bordereau BROUILLON. */
    DataResponse<BordereauCoursierResponse> ajouterCourrier(
        UUID bordereauId, UUID courrierId, Integer ordre, String loginAuteur);

    /** Retirer un courrier d'un bordereau BROUILLON. */
    DataResponse<BordereauCoursierResponse> retirerCourrier(
        UUID bordereauId, UUID courrierId, String loginAuteur);
}
