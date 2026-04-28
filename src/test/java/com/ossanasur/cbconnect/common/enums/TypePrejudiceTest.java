package com.ossanasur.cbconnect.common.enums;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class TypePrejudiceTest {

    @Test
    void libelles_fr_sont_corrects() {
        assertThat(TypePrejudice.MATERIEL.getLibelle()).isEqualTo("Préjudice matériel");
        assertThat(TypePrejudice.CORPOREL.getLibelle()).isEqualTo("Préjudice corporel");
        assertThat(TypePrejudice.MORAL.getLibelle()).isEqualTo("Préjudice moral");
    }

    @Test
    void enum_a_exactement_3_valeurs() {
        assertThat(TypePrejudice.values()).hasSize(3);
    }
}
