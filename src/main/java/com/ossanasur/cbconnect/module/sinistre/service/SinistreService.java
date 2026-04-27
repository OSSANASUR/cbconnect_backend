package com.ossanasur.cbconnect.module.sinistre.service;

import com.ossanasur.cbconnect.module.sinistre.dto.request.ConfirmationGarantieRequest;
import com.ossanasur.cbconnect.module.sinistre.dto.request.MiseEnArbitrageRequest;
import com.ossanasur.cbconnect.module.sinistre.dto.request.MiseEnContentieuxRequest;
import com.ossanasur.cbconnect.module.sinistre.dto.request.SinistreRequest;
import com.ossanasur.cbconnect.module.sinistre.dto.response.EncaissementStatusResponse;
import com.ossanasur.cbconnect.module.sinistre.dto.response.SinistreResponse;
import com.ossanasur.cbconnect.utils.DataResponse;
import com.ossanasur.cbconnect.utils.PaginatedResponse;
import java.util.UUID;

public interface SinistreService {
    DataResponse<SinistreResponse> create(SinistreRequest r, String loginAuteur);

    DataResponse<SinistreResponse> update(UUID id, SinistreRequest r, String loginAuteur);

    DataResponse<SinistreResponse> getByTrackingId(UUID id);

    PaginatedResponse<SinistreResponse> getAll(int page, int size);

    PaginatedResponse<SinistreResponse> search(String query, int page, int size);

    DataResponse<Void> changerStatut(UUID id, String nouveauStatut, String loginAuteur);

    DataResponse<SinistreResponse> confirmerGarantie(UUID id, ConfirmationGarantieRequest r, String loginAuteur);

    DataResponse<SinistreResponse> changerPositionRc(UUID id, String positionRc, String loginAuteur);

    DataResponse<Void> assignerRedacteur(UUID sinistreId, UUID redacteurId, String loginAuteur);

    DataResponse<Void> delete(UUID id, String loginAuteur);

    /* ═════════ Litige / sortie de litige ═════════ */
    DataResponse<SinistreResponse> mettreEnContentieux(UUID id, MiseEnContentieuxRequest r, String loginAuteur);

    DataResponse<SinistreResponse> mettreEnArbitrage(UUID id, MiseEnArbitrageRequest r, String loginAuteur);

    /** Sort le dossier du contentieux/arbitrage et le bascule en BAP (prêt à payer). */
    DataResponse<SinistreResponse> sortirDuLitige(UUID id, String loginAuteur);

    DataResponse<EncaissementStatusResponse> getEncaissementStatus(java.util.UUID sinistreTrackingId);
}
