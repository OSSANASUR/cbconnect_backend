package com.ossanasur.cbconnect.module.finance.entity;
import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.StatutCheque;
import com.ossanasur.cbconnect.module.auth.entity.Organisme;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import jakarta.persistence.*;
import lombok.*; import lombok.experimental.SuperBuilder;
import java.io.Serializable; import java.math.BigDecimal; import java.time.LocalDate; import java.util.UUID;
@RequiredArgsConstructor @AllArgsConstructor @Getter @Setter @SuperBuilder
@Entity @DiscriminatorValue("ENCAISSEMENT")
public class Encaissement extends InternalHistorique implements Serializable {
    @Column(name="encaissement_tracking_id",unique=true) private UUID encaissementTrackingId;
    @Column(nullable=false) private String numeroCheque;
    @Column(nullable=false) private BigDecimal montantCheque;
    @Column(nullable=false) private BigDecimal montantTheorique;
    @Builder.Default private BigDecimal produitFraisGestion=BigDecimal.ZERO;
    @Column(nullable=false) private LocalDate dateEmission;
    private LocalDate dateReception; private LocalDate dateEncaissement;
    private String banqueEmettrice;
    @Enumerated(EnumType.STRING) @Column(nullable=false) @Builder.Default private StatutCheque statutCheque=StatutCheque.RECU;
    private String motifAnnulation;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="organisme_emetteur_id",nullable=false) private Organisme organismeEmetteur;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="sinistre_id",nullable=false) private Sinistre sinistre;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="annule_par_id") private Utilisateur annulePar;
}
