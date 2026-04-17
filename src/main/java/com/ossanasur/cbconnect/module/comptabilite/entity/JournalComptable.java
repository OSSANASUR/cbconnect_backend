package com.ossanasur.cbconnect.module.comptabilite.entity;
import com.ossanasur.cbconnect.common.enums.TypeJournalComptable;
import jakarta.persistence.*;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name="journal_comptable")
public class JournalComptable {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Integer id;
    @Column(unique=true,nullable=false,length=10) private String codeJournal;
    @Column(nullable=false) private String libelleJournal;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private TypeJournalComptable type;
    @Builder.Default private boolean actif=true;
}
