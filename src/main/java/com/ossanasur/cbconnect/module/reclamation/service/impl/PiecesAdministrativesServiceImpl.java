package com.ossanasur.cbconnect.module.reclamation.service.impl;

import com.ossanasur.cbconnect.common.enums.StatutPiece;
import com.ossanasur.cbconnect.common.enums.StatutSinistre;
import com.ossanasur.cbconnect.common.enums.TypeDommage;
import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.module.ged.entity.OssanGedDocument;
import com.ossanasur.cbconnect.module.ged.repository.OssanGedDocumentRepository;
import com.ossanasur.cbconnect.module.reclamation.dto.request.AssocierDocumentRequest;
import com.ossanasur.cbconnect.module.reclamation.dto.request.TypePieceRequest;
import com.ossanasur.cbconnect.module.reclamation.dto.response.MaturiteDossierResponse;
import com.ossanasur.cbconnect.module.reclamation.dto.response.PieceDossierResponse;
import com.ossanasur.cbconnect.module.reclamation.dto.response.TypePieceResponse;
import com.ossanasur.cbconnect.module.reclamation.entity.DossierReclamation;
import com.ossanasur.cbconnect.module.reclamation.entity.PieceDossierReclamation;
import com.ossanasur.cbconnect.module.reclamation.entity.TypePieceAdministrative;
import com.ossanasur.cbconnect.module.reclamation.repository.DossierReclamationRepository;
import com.ossanasur.cbconnect.module.reclamation.repository.PieceDossierReclamationRepository;
import com.ossanasur.cbconnect.module.reclamation.repository.TypePieceAdministrativeRepository;
import com.ossanasur.cbconnect.module.reclamation.service.PiecesAdministrativesService;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import com.ossanasur.cbconnect.module.sinistre.repository.SinistreRepository;
import com.ossanasur.cbconnect.utils.DataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PiecesAdministrativesServiceImpl implements PiecesAdministrativesService {

        private final TypePieceAdministrativeRepository typePieceRepo;
        private final PieceDossierReclamationRepository pieceDossierRepo;
        private final DossierReclamationRepository dossierRepo;
        private final OssanGedDocumentRepository ossanGedDocRepo;
        private final SinistreRepository sinistreRepository;

        // ── Paramétrage ───────────────────────────────────────────────

        @Override
        @Transactional(readOnly = true)
        public DataResponse<List<TypePieceResponse>> listerTypesPieces() {
                return DataResponse.success("Types de pièces",
                                typePieceRepo.findAllActif().stream().map(this::toTypePieceResponse).toList());
        }

        @Override
        public DataResponse<TypePieceResponse> creerTypePiece(TypePieceRequest req, String loginAuteur) {
                TypePieceAdministrative t = TypePieceAdministrative.builder()
                                .trackingId(UUID.randomUUID())
                                .libelle(req.libelle().trim())
                                .typeDommage(req.typeDommage())
                                .obligatoire(req.obligatoire())
                                .ordre(req.ordre())
                                .actif(req.actif())
                                .createdBy(loginAuteur)
                                .activeData(true).deletedData(false)
                                .build();
                return DataResponse.success("Type de pièce créé", toTypePieceResponse(typePieceRepo.save(t)));
        }

        @Override
        public DataResponse<TypePieceResponse> modifierTypePiece(UUID trackingId, TypePieceRequest req,
                        String loginAuteur) {
                TypePieceAdministrative t = typePieceRepo.findByTrackingId(trackingId)
                                .orElseThrow(() -> new RessourceNotFoundException("Type de pièce introuvable"));
                t.setLibelle(req.libelle().trim());
                t.setTypeDommage(req.typeDommage());
                t.setObligatoire(req.obligatoire());
                t.setOrdre(req.ordre());
                t.setActif(req.actif());
                t.setUpdatedBy(loginAuteur);
                return DataResponse.success("Type de pièce modifié", toTypePieceResponse(typePieceRepo.save(t)));
        }

        @Override
        public DataResponse<Void> toggleActif(UUID trackingId, String loginAuteur) {
                TypePieceAdministrative t = typePieceRepo.findByTrackingId(trackingId)
                                .orElseThrow(() -> new RessourceNotFoundException("Type de pièce introuvable"));
                t.setActif(!t.isActif());
                t.setUpdatedBy(loginAuteur);
                typePieceRepo.save(t);
                return DataResponse.success(t.isActif() ? "Type de pièce activé" : "Type de pièce désactivé", null);
        }

        // ── Gestion par dossier ───────────────────────────────────────

        @Override
        public void initialiserPiecesDossier(UUID dossierTrackingId, String loginAuteur) {
                DossierReclamation dossier = dossierRepo.findActiveByTrackingId(dossierTrackingId)
                                .orElseThrow(() -> new RessourceNotFoundException("Dossier introuvable"));

                TypeDommage td = dossier.getSinistre().getTypeDommage();
                List<TypePieceAdministrative> types = typePieceRepo.findApplicablesPour(td);

                for (TypePieceAdministrative type : types) {
                        // Ne pas dupliquer si déjà initialisé
                        if (pieceDossierRepo.existsByDossierReclamation_HistoriqueIdAndTypePiece_HistoriqueId(
                                        dossier.getHistoriqueId(), type.getHistoriqueId())) {
                                continue;
                        }
                        PieceDossierReclamation piece = PieceDossierReclamation.builder()
                                        .trackingId(UUID.randomUUID())
                                        .dossierReclamation(dossier)
                                        .typePiece(type)
                                        .statut(StatutPiece.ATTENDUE)
                                        .createdBy(loginAuteur)
                                        .activeData(true).deletedData(false)
                                        .build();
                        pieceDossierRepo.save(piece);
                }
                log.info("[PIECES] {} pièces initialisées pour dossier {}", types.size(), dossier.getNumeroDossier());
        }

        @Override
        @Transactional(readOnly = true)
        public DataResponse<MaturiteDossierResponse> getMaturiteDossier(UUID dossierTrackingId) {
                DossierReclamation dossier = dossierRepo.findActiveByTrackingId(dossierTrackingId)
                                .orElseThrow(() -> new RessourceNotFoundException("Dossier introuvable"));

                List<PieceDossierReclamation> pieces = pieceDossierRepo.findByDossier(dossier.getHistoriqueId());

                List<PieceDossierResponse> piecesDto = pieces.stream().map(this::toPieceDossierResponse).toList();

                long nbRequises = pieces.stream().filter(p -> p.getTypePiece().isObligatoire()).count();
                long nbRecues = pieces.stream().filter(p -> p.getStatut() == StatutPiece.RECUE).count();
                long nbAttendues = pieces.stream().filter(p -> p.getStatut() == StatutPiece.ATTENDUE).count();
                long nbRejetees = pieces.stream().filter(p -> p.getStatut() == StatutPiece.REJETEE).count();
                boolean estMur = pieceDossierRepo.isDossierMur(dossier.getHistoriqueId());

                return DataResponse.success("Maturité dossier", new MaturiteDossierResponse(
                                dossier.getNumeroDossier(), estMur, nbRequises, nbRecues, nbAttendues, nbRejetees,
                                piecesDto));
        }

        @Override
        public DataResponse<PieceDossierResponse> associerDocument(
                        UUID pieceDossierTrackingId, AssocierDocumentRequest req, String loginAuteur) {

                PieceDossierReclamation piece = pieceDossierRepo.findByTrackingId(pieceDossierTrackingId)
                                .orElseThrow(() -> new RessourceNotFoundException("Pièce dossier introuvable"));

                OssanGedDocument doc = ossanGedDocRepo
                                .findByOssanGedDocumentTrackingId(req.ossanGedDocumentTrackingId())
                                .orElseThrow(() -> new RessourceNotFoundException("Document GED introuvable"));

                piece.setOssanGedDocument(doc);
                piece.setStatut(StatutPiece.RECUE);
                piece.setDateReception(LocalDate.now());
                piece.setNotes(req.notes());
                piece.setUpdatedBy(loginAuteur);

                log.info("[PIECES] Doc GED {} associé à pièce {} (dossier {})",
                                doc.getOssanGedDocumentId(),
                                piece.getTypePiece().getLibelle(),
                                piece.getDossierReclamation().getNumeroDossier());

                PieceDossierReclamation saved = pieceDossierRepo.save(piece);

                // Auto-transition du sinistre vers MUR dès que toutes les pièces obligatoires sont reçues
                autoTransitionVersMur(saved.getDossierReclamation(), loginAuteur);

                return DataResponse.success("Document associé", toPieceDossierResponse(saved));
        }

        /**
         * Règle métier : quand toutes les pièces obligatoires d'un dossier de réclamation
         * sont reçues, le sinistre associé passe automatiquement en statut MUR
         * (dossier mûr, prêt pour le calcul d'offre CIMA).
         * Idempotent — n'a d'effet que si le sinistre est exactement en ATTENTE_PIECES_DE_RECLAMATION.
         */
        private void autoTransitionVersMur(DossierReclamation dossier, String loginAuteur) {
                if (dossier == null) return;
                if (!pieceDossierRepo.isDossierMur(dossier.getHistoriqueId())) return;
                Sinistre sinistre = dossier.getSinistre();
                if (sinistre == null) return;
                if (sinistre.getStatut() != StatutSinistre.ATTENTE_PIECES_DE_RECLAMATION) return;
                sinistre.setStatut(StatutSinistre.MUR);
                sinistre.setUpdatedBy(loginAuteur);
                sinistreRepository.save(sinistre);
                log.info("[WORKFLOW] Sinistre {} → MUR (toutes pièces reçues sur dossier {})",
                                sinistre.getSinistreTrackingId(), dossier.getNumeroDossier());
        }

        @Override
        public DataResponse<PieceDossierResponse> rejeterDocument(
                        UUID pieceDossierTrackingId, String notes, String loginAuteur) {

                PieceDossierReclamation piece = pieceDossierRepo.findByTrackingId(pieceDossierTrackingId)
                                .orElseThrow(() -> new RessourceNotFoundException("Pièce dossier introuvable"));

                piece.setStatut(StatutPiece.REJETEE);
                piece.setNotes(notes);
                piece.setUpdatedBy(loginAuteur);

                PieceDossierReclamation saved = pieceDossierRepo.save(piece);
                // Si le sinistre était déjà MUR, il redescend à ATTENTE_PIECES_DE_RECLAMATION.
                rollbackMurSiNecessaire(saved.getDossierReclamation(), loginAuteur);

                return DataResponse.success("Document rejeté", toPieceDossierResponse(saved));
        }

        @Override
        public DataResponse<PieceDossierResponse> retirerDocument(
                        UUID pieceDossierTrackingId, String loginAuteur) {

                PieceDossierReclamation piece = pieceDossierRepo.findByTrackingId(pieceDossierTrackingId)
                                .orElseThrow(() -> new RessourceNotFoundException("Pièce dossier introuvable"));

                piece.setOssanGedDocument(null);
                piece.setStatut(StatutPiece.ATTENDUE);
                piece.setDateReception(null);
                piece.setUpdatedBy(loginAuteur);

                PieceDossierReclamation saved = pieceDossierRepo.save(piece);
                // Si le sinistre était déjà MUR, il redescend à ATTENTE_PIECES_DE_RECLAMATION.
                rollbackMurSiNecessaire(saved.getDossierReclamation(), loginAuteur);

                return DataResponse.success("Document retiré", toPieceDossierResponse(saved));
        }

        /**
         * Symétrique de {@link #autoTransitionVersMur}. Si on retire ou rejette une pièce
         * qui rendait le dossier mûr, le sinistre redescend à ATTENTE_PIECES_DE_RECLAMATION
         * pour attendre la nouvelle pièce — tant qu'il était MUR (pas plus loin dans le workflow).
         */
        private void rollbackMurSiNecessaire(DossierReclamation dossier, String loginAuteur) {
                if (dossier == null) return;
                if (pieceDossierRepo.isDossierMur(dossier.getHistoriqueId())) return;
                Sinistre sinistre = dossier.getSinistre();
                if (sinistre == null) return;
                if (sinistre.getStatut() != StatutSinistre.MUR) return;
                sinistre.setStatut(StatutSinistre.ATTENTE_PIECES_DE_RECLAMATION);
                sinistre.setUpdatedBy(loginAuteur);
                sinistreRepository.save(sinistre);
                log.info("[WORKFLOW] Sinistre {} → ATTENTE_PIECES_DE_RECLAMATION (rollback, pièce retirée/rejetée)",
                                sinistre.getSinistreTrackingId());
        }

        // ── Mappers privés ────────────────────────────────────────────

        private TypePieceResponse toTypePieceResponse(TypePieceAdministrative t) {
                String label = t.getTypeDommage() == null ? "Commun"
                                : switch (t.getTypeDommage()) {
                                        case CORPOREL -> "Corporel";
                                        case MATERIEL -> "Matériel";
                                        case MIXTE -> "Mixte";
                                };
                return new TypePieceResponse(t.getTrackingId(), t.getLibelle(),
                                t.getTypeDommage(), label, t.isObligatoire(), t.getOrdre(), t.isActif());
        }

        private PieceDossierResponse toPieceDossierResponse(PieceDossierReclamation p) {
                var doc = p.getOssanGedDocument();
                return new PieceDossierResponse(
                                p.getTrackingId(),
                                p.getTypePiece().getTrackingId(),
                                p.getTypePiece().getLibelle(),
                                p.getTypePiece().getTypeDommage(),
                                p.getTypePiece().isObligatoire(),
                                p.getTypePiece().getOrdre(),
                                p.getStatut(),
                                p.getDateReception(),
                                p.getNotes(),
                                doc != null ? doc.getOssanGedDocumentTrackingId() : null,
                                doc != null ? doc.getTitre() : null,
                                doc != null ? doc.getOssanGedDocumentId() : null);
        }
}
