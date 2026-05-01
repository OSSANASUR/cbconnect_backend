package com.ossanasur.cbconnect.module.reclamation.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.TypeDocumentOssanGed;
import com.ossanasur.cbconnect.common.enums.TypeDommage;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * Paramétrage des types de pièces administratives requises par dossier de
 * réclamation.
 *
 * typeDommage = null → pièce COMMUNE, requise pour tous les dossiers
 * typeDommage = CORPOREL | MATERIEL | MIXTE → pièce spécifique à ce type
 *
 * La maturité d'un dossier est calculée en vérifiant que toutes les pièces
 * obligatoires (COMMUN + matching typeDommage) ont statut = RECUE.
 */
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@DiscriminatorValue("TYPE_PIECE_ADMINISTRATIVE")
@Table(name = "type_piece_administrative")
public class TypePieceAdministrative extends InternalHistorique {

    @Column(name = "tracking_id", unique = true)
    private UUID trackingId;

    /**
     * Libellé affiché à l'utilisateur. Ex: "CNI", "PV Police", "Certificat médical"
     */
    @Column(nullable = false, length = 150)
    private String libelle;

    /**
     * Type de dommage concerné.
     * NULL = COMMUN (applicable à tous les TypeDommage).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type_dommage", length = 20)
    private TypeDommage typeDommage;

    /**
     * Si true, le dossier ne peut pas être déclaré mûr sans cette pièce.
     * Si false, la pièce est souhaitée mais non bloquante.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean obligatoire = true;

    /** Ordre d'affichage dans la checklist — le plus petit s'affiche en premier */
    @Column(nullable = false)
    @Builder.Default
    private int ordre = 0;

    /** Permet de désactiver un type de pièce sans le supprimer */
    @Column(nullable = false)
    @Builder.Default
    private boolean actif = true;

    /**
     * Type de document GED correspondant à cette pièce.
     * Quand renseigné, l'upload d'un document GED de ce type vers le dossier
     * déclenche l'auto-association : la pièce passe automatiquement à RECUE.
     * NULL = aucune auto-association (association manuelle uniquement).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type_document_ged", length = 30)
    private TypeDocumentOssanGed typeDocumentGed;
}
