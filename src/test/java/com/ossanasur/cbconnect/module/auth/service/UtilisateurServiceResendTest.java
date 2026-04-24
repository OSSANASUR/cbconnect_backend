package com.ossanasur.cbconnect.module.auth.service;

import com.ossanasur.cbconnect.exception.AlreadyExistException;
import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.module.auth.repository.UtilisateurRepository;
import com.ossanasur.cbconnect.module.auth.service.impl.UtilisateurServiceImpl;
import com.ossanasur.cbconnect.security.service.EmailSenderService;
import com.ossanasur.cbconnect.utils.DataResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UtilisateurServiceResendTest {

    @Mock UtilisateurRepository utilisateurRepository;
    @Mock EmailSenderService senderService;
    @Mock ParametreService parametreService;
    @InjectMocks UtilisateurServiceImpl service;

    @Test
    void resend_regenereTokenEtRenvoieMail() {
        UUID id = UUID.randomUUID();
        String oldToken = "ancien";
        Utilisateur u = Utilisateur.builder()
                .utilisateurTrackingId(id)
                .nom("Doe").prenoms("John").email("a@b.c")
                .accountSetupToken(oldToken)
                .isActive(false)
                .build();
        when(utilisateurRepository.findByUtilisateurTrackingIdAndActiveDataTrueAndDeletedDataFalse(id))
                .thenReturn(Optional.of(u));
        when(utilisateurRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(parametreService.getValeur(eq("ACCOUNT_SETUP_TOKEN_TTL_DAYS"), anyString())).thenReturn("7");
        when(parametreService.getValeur(eq("MAIL_FRONTEND_BASE_URL"), anyString())).thenReturn("http://localhost:3000");
        when(parametreService.getValeur(startsWith("MAIL_"), anyString())).thenReturn("x");

        DataResponse<Void> res = service.resendActivationLink(id, "admin@x");

        assertThat(res.isSuccess()).isTrue();
        ArgumentCaptor<Utilisateur> userCaptor = ArgumentCaptor.forClass(Utilisateur.class);
        verify(utilisateurRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getAccountSetupToken()).isNotEqualTo(oldToken);
        assertThat(userCaptor.getValue().getAccountSetupToken()).isNotBlank();

        verify(senderService).sendTemplated(eq("a@b.c"), anyString(), eq("registration"), any(Map.class));
    }

    @Test
    void resend_throwsWhenAlreadyActivated() {
        UUID id = UUID.randomUUID();
        Utilisateur u = Utilisateur.builder().utilisateurTrackingId(id).isActive(true).build();
        when(utilisateurRepository.findByUtilisateurTrackingIdAndActiveDataTrueAndDeletedDataFalse(id))
                .thenReturn(Optional.of(u));

        assertThatThrownBy(() -> service.resendActivationLink(id, "admin"))
                .isInstanceOf(AlreadyExistException.class);
    }

    @Test
    void resend_throwsWhenUserNotFound() {
        UUID id = UUID.randomUUID();
        when(utilisateurRepository.findByUtilisateurTrackingIdAndActiveDataTrueAndDeletedDataFalse(id))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resendActivationLink(id, "admin"))
                .isInstanceOf(RessourceNotFoundException.class);
    }
}
