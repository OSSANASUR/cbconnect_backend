package com.ossanasur.cbconnect.module.delai.entity;
import com.ossanasur.cbconnect.common.enums.StatutPenalite;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import com.ossanasur.cbconnect.module.sinistre.entity.Victime;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal; import java.time.LocalDate;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name="penalite_calculee")
public class PenaliteCalculee {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="sinistre_id",nullable=false) private Sinistre sinistre;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="victime_id") private Victime victime;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="parametre_delai_id",nullable=false) private ParametreDelai parametreDelai;
    @Column(nullable=false) private LocalDate dateCalcul;
    @Column(nullable=false) private BigDecimal montantBase;
    @Column(nullable=false) private BigDecimal tauxPctMois;
    @Column(nullable=false) private Integer nombreMoisRetard;
    @Column(nullable=false) private BigDecimal montantPenalite;
    @Enumerated(EnumType.STRING) @Column(nullable=false) @Builder.Default private StatutPenalite statut=StatutPenalite.CALCULEE;
    private String motifAnnulation;
}
