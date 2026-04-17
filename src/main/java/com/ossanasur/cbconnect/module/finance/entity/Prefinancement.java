package com.ossanasur.cbconnect.module.finance.entity;
import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import jakarta.persistence.*;
import lombok.*; import lombok.experimental.SuperBuilder;
import java.io.Serializable; import java.math.BigDecimal; import java.time.LocalDate; import java.util.UUID;
@RequiredArgsConstructor @AllArgsConstructor @Getter @Setter @SuperBuilder
@Entity @DiscriminatorValue("PREFINANCEMENT")
public class Prefinancement extends InternalHistorique implements Serializable {
    @Column(name="prefinancement_tracking_id",unique=true) private UUID prefinancementTrackingId;
    @Column(nullable=false) private BigDecimal montantPrefinance;
    @Column(nullable=false) private LocalDate datePrefinancement;
    private LocalDate dateRemboursement; @Builder.Default private boolean estRembourse=false;
    @Builder.Default private BigDecimal montantRembourse=BigDecimal.ZERO;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="sinistre_id",nullable=false) private Sinistre sinistre;
}
