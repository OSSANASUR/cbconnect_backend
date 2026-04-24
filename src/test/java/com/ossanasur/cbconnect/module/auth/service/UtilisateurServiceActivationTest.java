package com.ossanasur.cbconnect.module.auth.service;

import com.ossanasur.cbconnect.exception.AlreadyExistException;
import com.ossanasur.cbconnect.exception.LinkExpiredException;
import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.module.auth.repository.UtilisateurRepository;
import com.ossanasur.cbconnect.module.auth.service.impl.UtilisateurServiceImpl;
import com.ossanasur.cbconnect.security.dto.request.ActivateAccountRequest;
import com.ossanasur.cbconnect.security.dto.response.ActivationInfoResponse;
import com.ossanasur.cbconnect.security.entity.Passwords;
import com.ossanasur.cbconnect.security.repository.PasswordRepository;
import com.ossanasur.cbconnect.utils.DataResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UtilisateurServiceActivationTest {

    @Mock UtilisateurRepository utilisateurRepository;
    @Mock PasswordRepository passwordRepository;
    @Mock BCryptPasswordEncoder passwordEncoder;
    @Mock ParametreService parametreService;
    @InjectMocks UtilisateurServiceImpl service;

    private Utilisateur valideUser(String token) {
        return Utilisateur.builder()
                .utilisateurTrackingId(UUID.randomUUID())
                .nom("Doe").prenoms("John").email("a@b.c")
                .accountSetupToken(token)
                .accountSetupTokenExpiresAt(LocalDateTime.now().plusDays(3))
                .isActive(false)
                .build();
    }

    @Test
    void validate_returnsInfoWhenValid() {
        when(utilisateurRepository.findByAccountSetupToken("tok")).thenReturn(Optional.of(valideUser("tok")));
        when(parametreService.getValeur(eq("MAIL_SUPPORT_EMAIL"), anyString())).thenReturn("support@x");

        ActivationInfoResponse info = service.validateActivationToken("tok");

        assertThat(info.email()).isEqualTo("a@b.c");
        assertThat(info.nomComplet()).isEqualTo("Doe John");
        assertThat(info.supportEmail()).isEqualTo("support@x");
    }

    @Test
    void validate_throwsWhenTokenInconnu() {
        when(utilisateurRepository.findByAccountSetupToken("inconnu")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.validateActivationToken("inconnu"))
                .isInstanceOf(RessourceNotFoundException.class);
    }

    @Test
    void validate_throwsLinkExpiredWhenExpired() {
        Utilisateur u = valideUser("tok");
        u.setAccountSetupTokenExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(utilisateurRepository.findByAccountSetupToken("tok")).thenReturn(Optional.of(u));

        assertThatThrownBy(() -> service.validateActivationToken("tok"))
                .isInstanceOf(LinkExpiredException.class);
    }

    @Test
    void validate_throwsAlreadyExistWhenAlreadyActivated() {
        Utilisateur u = valideUser("tok");
        u.setActive(true);
        when(utilisateurRepository.findByAccountSetupToken("tok")).thenReturn(Optional.of(u));

        assertThatThrownBy(() -> service.validateActivationToken("tok"))
                .isInstanceOf(AlreadyExistException.class);
    }

    @Test
    void activerCompte_creeMotDePasseEtConsommeToken() {
        Utilisateur u = valideUser("tok");
        when(utilisateurRepository.findByAccountSetupToken("tok")).thenReturn(Optional.of(u));
        when(passwordEncoder.encode("Secret123!")).thenReturn("HASH");

        DataResponse<Void> res = service.activerCompte(new ActivateAccountRequest("tok", "Secret123!"));

        assertThat(res.isSuccess()).isTrue();
        ArgumentCaptor<Passwords> pwdCaptor = ArgumentCaptor.forClass(Passwords.class);
        verify(passwordRepository).save(pwdCaptor.capture());
        assertThat(pwdCaptor.getValue().getPassword()).isEqualTo("HASH");
        assertThat(pwdCaptor.getValue().isTemporary()).isFalse();

        ArgumentCaptor<Utilisateur> userCaptor = ArgumentCaptor.forClass(Utilisateur.class);
        verify(utilisateurRepository).save(userCaptor.capture());
        Utilisateur saved = userCaptor.getValue();
        assertThat(saved.isActive()).isTrue();
        assertThat(saved.getAccountSetupToken()).isNull();
        assertThat(saved.getAccountSetupTokenExpiresAt()).isNull();
    }
}
