package com.ossanasur.cbconnect.module.sinistre.entity;
import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.*;
import com.ossanasur.cbconnect.module.auth.entity.Organisme;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.module.pays.entity.Pays;
import jakarta.persistence.*;
import lombok.*; import lombok.experimental.SuperBuilder;
import java.io.Serializable; import java.math.BigDecimal; import java.time.LocalDate; import java.util.UUID;
@RequiredArgsConstructor @AllArgsConstructor @Getter @Setter @SuperBuilder
@Entity @DiscriminatorValue("SINISTRE")
public class Sinistre extends InternalHistorique implements Serializable {
    @Column(name="sinistre_tracking_id",unique=true) private UUID sinistreTrackingId;
    @Column(unique=true,nullable=false,length=30) private String numeroSinistreLocal;
    private String numeroSinistreManuel;
    private String numeroSinistreHomologue;
    private String numeroSinistreEcarteBrune;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private TypeSinistre typeSinistre;
    @Enumerated(EnumType.STRING) @Column(nullable=false) @Builder.Default private StatutSinistre statut=StatutSinistre.NOUVEAU;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private TypeDommage typeDommage;
    @Column(nullable=false) private LocalDate dateAccident;
    @Column(nullable=false) private LocalDate dateDeclaration;
    private String lieuAccident; private boolean agglomeration;
    private BigDecimal tauxRc;
    @Enumerated(EnumType.STRING) private PositionRc positionRc;
    @Builder.Default private boolean estPrefinance=false; @Builder.Default private boolean estContentieux=false;
    private String niveauJuridiction; private LocalDate dateProchaineAudience;
    private Long paperlessDossierId;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="pays_gestionnaire_id",nullable=false) private Pays paysGestionnaire;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="pays_emetteur_id") private Pays paysEmetteur;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="organisme_membre_id") private Organisme organismeMembre;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="organisme_homologue_id") private Organisme organismeHomologue;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="assure_id",nullable=false) private Assure assure;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="redacteur_id") private Utilisateur redacteur;
}
