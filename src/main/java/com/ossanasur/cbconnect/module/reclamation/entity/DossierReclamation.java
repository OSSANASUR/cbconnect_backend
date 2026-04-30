package com.ossanasur.cbconnect.module.reclamation.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.StatutDossierReclamation;
import com.ossanasur.cbconnect.common.enums.StatutReclamation;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import com.ossanasur.cbconnect.module.sinistre.entity.Victime;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@DiscriminatorValue("DOSSIER_RECLAMATION")
@Table(name = "dossier_reclamation")
public class DossierReclamation extends InternalHistorique {
    @Column(name = "dossier_reclamation_tracking_id", unique = true)
    private UUID dossierTrackingId;
    @Column(unique = true, nullable = false)
    private String numeroDossier;
    @Column(nullable = false)
    private LocalDate dateOuverture;
    private LocalDate dateCloture;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutDossierReclamation statut = StatutDossierReclamation.OUVERT;
    @Builder.Default
    private BigDecimal montantTotalReclame = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal montantTotalRetenu = BigDecimal.ZERO;
    private String notesRedacteur;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sinistre_id", nullable = false)
    private Sinistre sinistre;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "victime_id", nullable = false)
    private Victime victime;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "redacteur_id")
    private Utilisateur redacteur;
    /**
     * Statut de réclamation par victime.
     * Permet de distinguer l'état de chaque dossier indépendamment
     * du statut global du sinistre.
     * Ex : BAP pour une victime, ATTENTE_OFFRE pour une autre.
     */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "statut_reclamation", length = 30)
    @Builder.Default
    private StatutReclamation statutReclamation = StatutReclamation.AUTRES;
}
