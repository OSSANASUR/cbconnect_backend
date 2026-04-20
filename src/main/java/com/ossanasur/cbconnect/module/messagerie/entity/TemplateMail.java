package com.ossanasur.cbconnect.module.messagerie.entity;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.common.enums.TypeTemplateMailEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@DiscriminatorValue("TEMPLATE_MAIL")
@Table(name = "template_mail")
public class TemplateMail extends InternalHistorique {

    @Column(name = "tracking_id", unique = true)
    private UUID trackingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_template", nullable = false, length = 50)
    private TypeTemplateMailEnum typeTemplate;

    @Column(nullable = false, length = 200)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Sujet avec variables : {{numero_sinistre}}, {{organisme_homologue}}, etc. */
    @Column(nullable = false, length = 500)
    private String sujet;

    /** Corps HTML avec variables */
    @Column(name = "corps_html", nullable = false, columnDefinition = "TEXT")
    private String corpsHtml;

    @Column(nullable = false)
    @Builder.Default
    private boolean actif = true;
}
