package com.ossanasur.cbconnect.module.auth.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.TypeOrganisme;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Represente un Bureau National, un Bureau Homologue CEDEAO ou une Compagnie
 * Membre.
 * Remplace l'ancienne entite COMPAGNIE_ASSURANCE — discrimination via
 * typeOrganisme.
 */
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@DiscriminatorValue("ORGANISME")
public class Organisme extends InternalHistorique {

    @Column(name = "organisme_tracking_id", unique = true)
    private UUID organismeTrackingId;

    /** BUREAU_NATIONAL | BUREAU_HOMOLOGUE | COMPAGNIE_MEMBRE */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeOrganisme typeOrganisme;

    @Column(nullable = false)
    private String raisonSociale;

    /**
     * Code court utilise dans la numerotation des sinistres. Ex: BNCB-TG, BF, SN,
     * NSIA-TG
     */
    @Column(unique = true, nullable = false, length = 20)
    private String code;

    private String responsable;

    @Column(unique = true, nullable = false)
    private String email;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "organisme_contacts", joinColumns = @JoinColumn(name = "organisme_id"))
    @Column(name = "contact")
    private List<String> contacts;

    /** Code ISO alpha-3 du pays. Ex: TGO, BFA, SEN */
    @Column(length = 5)
    private String codePays;

    /** Code Carte Brune 2 lettres. Ex: TG, BF, SN */
    @Column(name = "code_pays_bcb", length = 5)
    private String codePaysBCB;

    /** FK logique vers PAYS (Integer ID) */
    private Integer paysId;

    private LocalDate dateCreation;
    private String numeroAgrement;
    private String apiEndpointUrl;
    private String logo;
    @Builder.Default
    private boolean active = true;

    // Flag pour distinguer les organismes importés via reprise historique BNCB.
    // Utile pour filtrer les rapports et compter les imports (voir
    // RepriseService.getStatut).
    @Column(name = "reprise_historique")
    @Builder.Default
    private boolean repriseHistorique = false;

    @Column(name = "two_factor_enabled", nullable = false)
    @Builder.Default
    private boolean twoFactorEnabled = false;
}
