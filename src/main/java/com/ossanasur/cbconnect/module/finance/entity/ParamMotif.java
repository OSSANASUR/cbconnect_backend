package com.ossanasur.cbconnect.module.finance.entity;

import java.util.UUID;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.TypeMotif;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@DiscriminatorValue("PARAM_MOTIF")
@Table(name = "param_motifs")
public class ParamMotif extends InternalHistorique {

    @Column(name = "param_motif_tracking_id", unique = true)
    private UUID paramMotifTrackingId;

    @Column(nullable = false, length = 150)
    private String libelleMotif;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TypeMotif type;

    @Column(nullable = false)
    @Builder.Default
    private Boolean actif = true;
}
