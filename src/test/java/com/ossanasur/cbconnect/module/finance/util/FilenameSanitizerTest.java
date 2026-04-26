package com.ossanasur.cbconnect.module.finance.util;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class FilenameSanitizerTest {

    @Test
    void remplace_espaces_par_underscores() {
        assertThat(FilenameSanitizer.sanitize("Clinique Kom Ombo")).isEqualTo("Clinique_Kom_Ombo");
    }

    @Test
    void remplace_separateurs_invalides_par_tirets() {
        assertThat(FilenameSanitizer.sanitize("SINTPRC0672/22")).isEqualTo("SINTPRC0672-22");
        assertThat(FilenameSanitizer.sanitize("a\\b:c*d?e\"f<g>h|i")).isEqualTo("a-b-c-d-e-f-g-h-i");
    }

    @Test
    void tronque_a_60_caracteres() {
        String input = "X".repeat(100);
        assertThat(FilenameSanitizer.sanitize(input)).hasSize(60);
    }

    @Test
    void retourne_inconnu_si_vide() {
        assertThat(FilenameSanitizer.sanitize(null)).isEqualTo("INCONNU");
        assertThat(FilenameSanitizer.sanitize("")).isEqualTo("INCONNU");
        assertThat(FilenameSanitizer.sanitize("   ")).isEqualTo("INCONNU");
    }

    @Test
    void preserve_lettres_chiffres_tirets_underscores() {
        assertThat(FilenameSanitizer.sanitize("ABC-123_xyz")).isEqualTo("ABC-123_xyz");
    }
}
