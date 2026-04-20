package com.ossanasur.cbconnect.module.reclamation.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.*;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@DiscriminatorValue("FACTURE_RECLAMATION")
@Table(name = "facture_reclamation")
public class FactureReclamation extends InternalHistorique {
    @Column(name = "facture_reclamation_tracking_id", unique = true)
    private UUID factureTrackingId;
    private String numeroFactureOriginal;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeDepenseReclamation typeDepense;
    @Column(nullable = false)
    private String nomPrestataire;
    @Column(nullable = false)
    private LocalDate dateFacture;
    @Column(nullable = false)
    private BigDecimal montantReclame;
    private BigDecimal montantRetenu;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutTraitementFacture statutTraitement = StatutTraitementFacture.EN_ATTENTE;
    private String motifRejet;
    @Builder.Default
    private boolean lienAvecAccidentVerifie = false;
    private LocalDate dateTraitement;
    @Column(name = "ossan_ged_document_id")
    private Long ossanGedDocumentId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_reclamation_id", nullable = false)
    private DossierReclamation dossierReclamation;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "traite_par_id")
    private Utilisateur traitePar;
}
