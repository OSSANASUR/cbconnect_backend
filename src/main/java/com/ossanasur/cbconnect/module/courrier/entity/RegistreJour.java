package com.ossanasur.cbconnect.module.courrier.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.StatutRegistre;
import com.ossanasur.cbconnect.common.enums.TypeRegistre;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Registre journalier des courriers (ARRIVEE ou DEPART) tenu par la secrétaire.
 * Un registre = (date_jour, type). Clôturé en fin de journée, imprimé puis
 * signé par le chef.
 */
@Entity
@Table(name = "registre_jour")
@DiscriminatorValue("REGISTRE_JOUR")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class RegistreJour extends InternalHistorique {

    @Column(name = "registre_tracking_id", nullable = false, unique = true)
    private UUID registreTrackingId;

    @Column(name = "date_jour", nullable = false)
    private LocalDate dateJour;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_registre", nullable = false, length = 10)
    private TypeRegistre typeRegistre;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 15)
    @Builder.Default
    private StatutRegistre statut = StatutRegistre.OUVERT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secretaire_id")
    private Utilisateur secretaire;

    @Column(name = "date_cloture")
    private LocalDateTime dateCloture;

    @Column(name = "clos_par")
    private String closPar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vise_par_id")
    private Utilisateur viseParUtilisateur;

    @Column(name = "date_visa")
    private LocalDateTime dateVisa;

    @Column(name = "commentaire_chef", columnDefinition = "TEXT")
    private String commentaireChef;

    @Column(name = "scan_ged_document_id")
    private Integer scanGedDocumentId;

    /** Courriers enregistrés dans ce registre du jour. */
    @OneToMany(mappedBy = "registreJour", fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<Courrier> courriers = new ArrayList<>();
}
