package com.ossanasur.cbconnect.module.courrier.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.*;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@DiscriminatorValue("COURRIER")
public class Courrier extends InternalHistorique {
    @Column(name = "courrier_tracking_id", unique = true)
    private UUID courrierTrackingId;
    @Column(unique = true, nullable = false)
    private String referenceCourrier;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeCourrier typeCourrier;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NatureCourrier nature;
    @Column(nullable = false)
    private String expediteur;
    @Column(nullable = false)
    private String destinataire;
    @Column(nullable = false)
    private String objet;
    @Column(nullable = false)
    private LocalDate dateCourrier;
    private LocalDate dateReception;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CanalCourrier canal = CanalCourrier.MAIL;
    private String referenceBordereau;
    @Builder.Default
    private boolean traite = false;
    private LocalDateTime dateTraitement;
    private Integer paperlessDocumentId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sinistre_id")
    private Sinistre sinistre;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "traite_par_id")
    private Utilisateur traitePar;
}
