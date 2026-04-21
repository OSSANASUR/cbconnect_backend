package com.ossanasur.cbconnect.module.reclamation.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.StatutPiece;
import com.ossanasur.cbconnect.module.ged.entity.OssanGedDocument;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Liaison entre un DossierReclamation, un TypePieceAdministrative et un
 * document GED.
 *
 * Cycle de vie d'une pièce :
 * 1. Dossier ouvert → pièces ATTENDUE créées automatiquement (via service)
 * 2. Doc scanné dans GED → apparaît dans la liste "documents disponibles"
 * 3. Opérateur associe le doc GED à cette pièce → statut passe à RECUE
 * 4. Si doc refusé → statut REJETEE, un nouveau doc peut être associé
 *
 * Un seul doc GED par type de pièce par dossier (contrainte UNIQUE en base).
 */
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@DiscriminatorValue("PIECE_DOSSIER")
@Table(name = "piece_dossier_reclamation", uniqueConstraints = @UniqueConstraint(columnNames = {
        "dossier_reclamation_id", "type_piece_id" }))
public class PieceDossierReclamation extends InternalHistorique {

    @Column(name = "tracking_id", unique = true)
    private UUID trackingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_reclamation_id", nullable = false)
    private DossierReclamation dossierReclamation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_piece_id", nullable = false)
    private TypePieceAdministrative typePiece;

    /**
     * Document GED associé.
     * NULL tant que la pièce n'est pas encore reçue et associée.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ossan_ged_document_id")
    private OssanGedDocument ossanGedDocument;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatutPiece statut = StatutPiece.ATTENDUE;

    /** Date à laquelle le document a été reçu et associé */
    private LocalDate dateReception;

    /** Notes de l'opérateur (ex: motif de rejet) */
    private String notes;
}
