package com.ossanasur.cbconnect.module.auth.service;

import com.ossanasur.cbconnect.common.enums.TypeParametre;
import com.ossanasur.cbconnect.module.auth.entity.Parametre;
import com.ossanasur.cbconnect.module.auth.repository.ParametreRepository;
import com.ossanasur.cbconnect.module.auth.service.impl.ParametreServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParametreServiceGetValeurTest {

    @Mock ParametreRepository parametreRepository;
    @InjectMocks ParametreServiceImpl service;

    @Test
    void getValeur_returnsValueWhenKeyExists() {
        Parametre p = Parametre.builder()
                .typeParametre(TypeParametre.MAIL)
                .cle("MAIL_FOOTER_TEL")
                .valeur("(+228) 22 22 39 55")
                .build();
        when(parametreRepository.findByCle("MAIL_FOOTER_TEL")).thenReturn(Optional.of(p));

        String v = service.getValeur("MAIL_FOOTER_TEL", "fallback");

        assertThat(v).isEqualTo("(+228) 22 22 39 55");
    }

    @Test
    void getValeur_returnsDefaultWhenKeyMissing() {
        when(parametreRepository.findByCle("INCONNU")).thenReturn(Optional.empty());

        String v = service.getValeur("INCONNU", "fallback");

        assertThat(v).isEqualTo("fallback");
    }
}
