package com.ossanasur.cbconnect.module.baremes.entity;
import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "bareme_pretium_doloris")
public class BaremePretiumDoloris {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
    @Column(nullable = false, length = 50, unique = true) private String qualification;
    @Column(nullable = false) private Integer points; // % SMIG annuel pour calcul
    @Column(nullable = false) @Builder.Default private boolean moral = false;
    @Column(nullable = false) @Builder.Default private boolean actif = true;
}
