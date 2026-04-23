package com.ossanasur.cbconnect.common.entity;

import com.ossanasur.cbconnect.common.enums.TypeTable;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Superclasse abstraite heritee par toutes les entites metier CBConnect.
 * Chaque modification = INSERT (nouvelle version) + desactivation de
 * l'ancienne.
 * Jamais de DELETE ni d'UPDATE destructif.
 */
@Data
@RequiredArgsConstructor
@MappedSuperclass
@SuperBuilder
@AllArgsConstructor
public abstract class InternalHistorique implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer historiqueId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    /** Login (String) de l'auteur — volontairement String, pas FK Long */
    private String createdBy;
    private String updatedBy;
    private String deletedBy;

    private String libelle;

    @Column(name = "active_data")
    @Builder.Default
    private boolean activeData = true;

    /** UUID metier (String) de la version precedente — null a la creation */
    private String parentCodeId;

    @Column(name = "deleted_data")
    @Builder.Default
    private boolean deletedData = false;

    /** Enum identifiant la table source pour les audits transversaux */
    @Enumerated(EnumType.STRING)
    private TypeTable fromTable;

    /** true si l'enregistrement vient d'un import Excel */
    @Builder.Default
    private boolean excel = false;

    @PrePersist
    protected void onPrePersist() {
        if (this.createdAt == null)
            this.createdAt = LocalDateTime.now();
    }
}
