package com.ossanasur.cbconnect.module.sinistre.entity;
import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.*;
import com.ossanasur.cbconnect.module.pays.entity.Pays;
import jakarta.persistence.*;
import lombok.*; import lombok.experimental.SuperBuilder;
import java.io.Serializable; import java.math.BigDecimal; import java.time.LocalDate; import java.util.UUID;
@RequiredArgsConstructor @AllArgsConstructor @Getter @Setter @SuperBuilder
@Entity @DiscriminatorValue("VICTIME")
public class Victime extends InternalHistorique implements Serializable {
    @Column(name="victime_tracking_id",unique=true) private UUID victimeTrackingId;
    @Column(nullable=false) private String nom;
    @Column(nullable=false) private String prenoms;
    @Column(nullable=false) private LocalDate dateNaissance;
    @Column(nullable=false,length=1) private String sexe;
    private String nationalite;
    @Enumerated(EnumType.STRING) @Column(nullable=false) @Builder.Default private TypeVictime typeVictime=TypeVictime.NEUTRE;
    @Enumerated(EnumType.STRING) @Column(nullable=false) @Builder.Default private StatutVictime statutVictime=StatutVictime.NEUTRE;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private StatutActivite statutActivite;
    @Column(nullable=false) @Builder.Default private BigDecimal revenuMensuel=BigDecimal.ZERO;
    @Builder.Default private boolean estDcdSuiteBlessures=false; private LocalDate dateDeces; @Builder.Default private boolean lienDecesAccident=false;
    private Integer paperlessCorrespondentId;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="sinistre_id",nullable=false) private Sinistre sinistre;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="pays_residence_id") private Pays paysResidence;
}
