package com.ossanasur.cbconnect.module.delai.entity;
import com.ossanasur.cbconnect.common.enums.*;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import com.ossanasur.cbconnect.module.sinistre.entity.Victime;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate; import java.time.LocalDateTime;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name="notification_delai")
public class NotificationDelai {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="parametre_delai_id",nullable=false) private ParametreDelai parametreDelai;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="sinistre_id",nullable=false) private Sinistre sinistre;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="victime_id") private Victime victime;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="responsable_id") private Utilisateur responsable;
    @Column(nullable=false) private LocalDate dateDebut;
    @Column(nullable=false) private LocalDate dateEcheance;
    @Enumerated(EnumType.STRING) @Column(nullable=false) @Builder.Default private StatutNotificationDelai statut=StatutNotificationDelai.EN_COURS;
    @Enumerated(EnumType.STRING) @Column(nullable=false) @Builder.Default private NiveauAlerteDelai niveauAlerte=NiveauAlerteDelai.NORMAL;
    private LocalDateTime dateResolution; private String motifResolution;
    @Builder.Default private Integer nombreAlertes=0; private LocalDateTime derniereAlerteEnvoyee;
}
