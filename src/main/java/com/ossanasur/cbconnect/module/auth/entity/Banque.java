package com.ossanasur.cbconnect.module.auth.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@Table(name = "banque")
@DiscriminatorValue("BANQUE")
public class Banque extends InternalHistorique {

    @Column(name = "banque_tracking_id", unique = true, nullable = false)
    private UUID banqueTrackingId;

    /** Raison sociale complète. Ex: ECOBANK TOGO */
    @Column(nullable = false)
    private String nom;

    /** Code court unique. Ex: ECOBKTG */
    @Column(unique = true, nullable = false, length = 20)
    private String code;

    /** Code BIC/SWIFT international */
    @Column(name = "code_bic", length = 15)
    private String codeBic;

    /** Agence ou succursale */
    private String agence;

    private String ville;

    /** Code pays ISO 3 lettres. Ex: TGO */
    @Column(name = "code_pays", length = 5)
    private String codePays;

    private String telephone;
}
