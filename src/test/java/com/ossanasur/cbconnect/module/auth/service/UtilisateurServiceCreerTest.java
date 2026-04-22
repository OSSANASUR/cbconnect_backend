package com.ossanasur.cbconnect.module.auth.service;

import com.ossanasur.cbconnect.exception.AlreadyExistException;
import com.ossanasur.cbconnect.historique.UtilisateurVersioningService;
import com.ossanasur.cbconnect.module.auth.dto.request.UtilisateurRequest;
import com.ossanasur.cbconnect.module.auth.dto.response.UtilisateurResponse;
import com.ossanasur.cbconnect.module.auth.entity.Organisme;
import com.ossanasur.cbconnect.module.auth.entity.Profil;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.module.auth.mapper.UtilisateurMapper;
import com.ossanasur.cbconnect.module.auth.repository.ProfilRepository;
import com.ossanasur.cbconnect.module.auth.repository.UtilisateurRepository;
import com.ossanasur.cbconnect.module.auth.service.impl.UtilisateurServiceImpl;
import com.ossanasur.cbconnect.security.repository.PasswordRepository;
import com.ossanasur.cbconnect.security.service.EmailSenderService;
import com.ossanasur.cbconnect.security.service.JwtService;
import com.ossanasur.cbconnect.security.service.TwoFactorAuthService;
import com.ossanasur.cbconnect.utils.DataResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UtilisateurServiceCreerTest {

    @Mock UtilisateurRepository utilisateurRepository;
    @Mock ProfilRepository profilRepository;
    @Mock PasswordRepository passwordRepository;
    @Mock UtilisateurMapper utilisateurMapper;
    @Mock UtilisateurVersioningService versioningService;
    @Mock JwtService jwtService;
    @Mock TwoFactorAuthService twoFactorAuthService;
    @Mock BCryptPasswordEncoder passwordEncoder;
    @Mock EmailSenderService senderService;
    @Mock ParametreService parametreService;

    @InjectMocks UtilisateurServiceImpl service;

    @Test
    void creer_genereTokenEtEnvoieMailAvecLien() {
        UUID profilId = UUID.randomUUID();
        Profil profil = Profil.builder()
                .profilTrackingId(profilId)
                .profilNom("REDACTEUR")
                .organisme(Organisme.builder().raisonSociale("BNCB Togo").build())
                .build();
        when(profilRepository.findActiveByTrackingId(profilId)).thenReturn(Optional.of(profil));
        when(utilisateurRepository.existsByEmailAndActiveDataTrueAndDeletedDataFalse("a@b.c")).thenReturn(false);
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(inv -> inv.getArgument(0));
        when(utilisateurMapper.toResponse(any())).thenReturn(mock(UtilisateurResponse.class));
        // ParametreService stubs
        when(parametreService.getValeur(eq("ACCOUNT_SETUP_TOKEN_TTL_DAYS"), anyString())).thenReturn("7");
        when(parametreService.getValeur(eq("MAIL_FRONTEND_BASE_URL"), anyString())).thenReturn("http://localhost:3000");
        when(parametreService.getValeur(startsWith("MAIL_FOOTER_"), anyString())).thenReturn("x");
        when(parametreService.getValeur(eq("MAIL_SUPPORT_EMAIL"), anyString())).thenReturn("support@x");

        UtilisateurRequest req = new UtilisateurRequest("Doe", "John", "a@b.c", null, null, profilId);

        DataResponse<UtilisateurResponse> res = service.creer(req, "admin@bncb");

        assertThat(res.isSuccess()).isTrue();
        ArgumentCaptor<Utilisateur> userCaptor = ArgumentCaptor.forClass(Utilisateur.class);
        verify(utilisateurRepository).save(userCaptor.capture());
        Utilisateur saved = userCaptor.getValue();
        assertThat(saved.isActive()).isFalse();
        assertThat(saved.getAccountSetupToken()).isNotBlank();
        assertThat(saved.getAccountSetupTokenExpiresAt()).isNotNull();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> varsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(senderService).sendTemplated(eq("a@b.c"), anyString(), eq("registration"), varsCaptor.capture());
        Map<String, Object> vars = varsCaptor.getValue();
        assertThat(vars.get("emailUtilisateur")).isEqualTo("a@b.c");
        assertThat(vars.get("nomComplet")).isEqualTo("Doe John");
        assertThat((String) vars.get("activationUrl")).contains("/activation/" + saved.getAccountSetupToken());

        // Aucun Passwords cree
        verifyNoInteractions(passwordRepository);
    }

    @Test
    void creer_emailDejaUtilise_throws() {
        when(utilisateurRepository.existsByEmailAndActiveDataTrueAndDeletedDataFalse("dup@b.c")).thenReturn(true);
        UtilisateurRequest req = new UtilisateurRequest("X", "Y", "dup@b.c", null, null, null);

        assertThatThrownBy(() -> service.creer(req, "admin"))
                .isInstanceOf(AlreadyExistException.class);
    }
}
