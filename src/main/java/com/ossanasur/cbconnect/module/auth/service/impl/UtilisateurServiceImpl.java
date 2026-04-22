package com.ossanasur.cbconnect.module.auth.service.impl;

import com.ossanasur.cbconnect.exception.AlreadyExistException;
import com.ossanasur.cbconnect.exception.BadRequestException;
import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.historique.UtilisateurVersioningService;
import com.ossanasur.cbconnect.module.auth.dto.request.UtilisateurRequest;
import com.ossanasur.cbconnect.module.auth.dto.response.UtilisateurResponse;
import com.ossanasur.cbconnect.module.auth.entity.Profil;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.module.auth.mapper.UtilisateurMapper;
import com.ossanasur.cbconnect.module.auth.repository.ProfilRepository;
import com.ossanasur.cbconnect.module.auth.repository.UtilisateurRepository;
import com.ossanasur.cbconnect.module.auth.service.ParametreService;
import com.ossanasur.cbconnect.module.auth.service.UtilisateurService;
import com.ossanasur.cbconnect.security.dto.request.ChangePasswordRequest;
import com.ossanasur.cbconnect.security.dto.request.LoginRequest;
import com.ossanasur.cbconnect.security.dto.response.LoginResponse;
import com.ossanasur.cbconnect.security.dto.response.UserInfoResponse;
import com.ossanasur.cbconnect.security.entity.Passwords;
import com.ossanasur.cbconnect.security.repository.PasswordRepository;
import com.ossanasur.cbconnect.security.service.EmailSenderService;
import com.ossanasur.cbconnect.security.service.JwtService;
import com.ossanasur.cbconnect.security.service.TwoFactorAuthService;
import com.ossanasur.cbconnect.utils.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UtilisateurServiceImpl implements UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final ProfilRepository profilRepository;
    private final PasswordRepository passwordRepository;
    private final UtilisateurMapper utilisateurMapper;
    private final UtilisateurVersioningService versioningService;
    private final JwtService jwtService;
    private final TwoFactorAuthService twoFactorAuthService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailSenderService senderService;
    private final ParametreService parametreService;

    @Override
    @Transactional
    public DataResponse<UtilisateurResponse> creer(UtilisateurRequest request, String loginAuteur) {
        if (utilisateurRepository.existsByEmailAndActiveDataTrueAndDeletedDataFalse(request.email()))
            throw new AlreadyExistException("L'email '" + request.email() + "' est deja utilise");

        Profil profil = null;
        if (request.profilTrackingId() != null) {
            profil = profilRepository.findActiveByTrackingId(request.profilTrackingId())
                    .orElseThrow(() -> new RessourceNotFoundException("Profil introuvable"));
        }

        int ttlDays = Integer.parseInt(parametreService.getValeur("ACCOUNT_SETUP_TOKEN_TTL_DAYS", "7"));
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(ttlDays);

        Utilisateur utilisateur = Utilisateur.builder()
                .utilisateurTrackingId(UUID.randomUUID())
                .nom(request.nom()).prenoms(request.prenoms()).email(request.email())
                .username(request.username() != null ? request.username() : request.email())
                .telephone(request.telephone()).profil(profil)
                .isActive(false)
                .mustChangePassword(false)
                .accountSetupToken(token)
                .accountSetupTokenExpiresAt(expiresAt)
                .createdBy(loginAuteur).activeData(true).deletedData(false)
                .fromTable(com.ossanasur.cbconnect.common.enums.TypeTable.UTILISATEUR)
                .build();
        Utilisateur saved = utilisateurRepository.save(utilisateur);

        sendActivationEmail(saved, token, expiresAt, ttlDays);

        return DataResponse.created("Utilisateur cree avec succes", utilisateurMapper.toResponse(saved));
    }

    private void sendActivationEmail(Utilisateur u, String token, LocalDateTime expiresAt, int ttlDays) {
        String frontUrl = parametreService.getValeur("MAIL_FRONTEND_BASE_URL", "http://localhost:3000");
        String roleLabel = (u.getProfil() != null && u.getProfil().getOrganisme() != null)
                ? u.getProfil().getProfilNom() + " — " + u.getProfil().getOrganisme().getRaisonSociale()
                : "—";
        String expirationLabel = expiresAt.format(
                DateTimeFormatter.ofPattern("d MMMM uuuu 'à' HH:mm", Locale.FRENCH));

        Map<String, Object> vars = new HashMap<>();
        vars.put("nomComplet",       u.getNom() + " " + u.getPrenoms());
        vars.put("emailUtilisateur", u.getEmail());
        vars.put("roleLabel",        roleLabel);
        vars.put("expirationLabel",  expirationLabel);
        vars.put("activationUrl",    frontUrl + "/activation/" + token);
        vars.put("ttlDays",          ttlDays);
        vars.put("footerOrganisme",  parametreService.getValeur("MAIL_FOOTER_ORGANISME",  "Carte Brune CEDEAO"));
        vars.put("footerAdresse",    parametreService.getValeur("MAIL_FOOTER_ADRESSE",    ""));
        vars.put("footerBp",         parametreService.getValeur("MAIL_FOOTER_BP",         ""));
        vars.put("footerTel",        parametreService.getValeur("MAIL_FOOTER_TEL",        ""));
        vars.put("footerEmail",      parametreService.getValeur("MAIL_FOOTER_EMAIL",      "contact@cartebrune.org"));
        vars.put("footerLogoUrl",    parametreService.getValeur("MAIL_FOOTER_LOGO_URL",   ""));
        vars.put("supportEmail",     parametreService.getValeur("MAIL_SUPPORT_EMAIL",     "support@bncb-togo.com"));

        senderService.sendTemplated(u.getEmail(),
                "Activation de votre compte CBConnect",
                "registration",
                vars);
    }

    @Override
    @Transactional
    public DataResponse<UtilisateurResponse> modifier(UUID id, UtilisateurRequest r, String loginAuteur) {
        Utilisateur updated = versioningService.createVersion(id, r, loginAuteur);
        return DataResponse.success("Utilisateur mis a jour", utilisateurMapper.toResponse(updated));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<UtilisateurResponse> getByTrackingId(UUID id) {
        Utilisateur u = utilisateurRepository.findByUtilisateurTrackingIdAndActiveDataTrueAndDeletedDataFalse(id)
                .orElseThrow(() -> new RessourceNotFoundException("Utilisateur introuvable"));
        return DataResponse.success(utilisateurMapper.toResponse(u));
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse<List<UtilisateurResponse>> getAll() {
        return DataResponse.success(utilisateurRepository.findAllActive()
                .stream().map(utilisateurMapper::toResponse).collect(Collectors.toList()));
    }

    @Override
    @Transactional
    public DataResponse<Void> supprimer(UUID id, String loginAuteur) {
        versioningService.softDelete(id, loginAuteur);
        return DataResponse.success("Utilisateur supprime", null);
    }

    @Override
    @Transactional
    public DataResponse<Void> changerPassword(UUID id, ChangePasswordRequest r, String loginAuteur) {
        Utilisateur u = utilisateurRepository.findByUtilisateurTrackingIdAndActiveDataTrueAndDeletedDataFalse(id)
                .orElseThrow(() -> new RessourceNotFoundException("Utilisateur introuvable"));
        Passwords pwd = passwordRepository.findActiveByUtilisateurTrackingId(u.getUtilisateurTrackingId());
        if (pwd == null || !passwordEncoder.matches(r.ancienPassword(), pwd.getPassword()))
            throw new BadRequestException("Ancien mot de passe incorrect");
        pwd.setActiveData(false);
        passwordRepository.save(pwd);
        Passwords newPwd = Passwords.builder()
                .passwordsTrackingId(UUID.randomUUID())
                .password(passwordEncoder.encode(r.nouveauPassword()))
                .isTemporary(false).utilisateur(u)
                .createdBy(loginAuteur).activeData(true).deletedData(false)
                .fromTable(com.ossanasur.cbconnect.common.enums.TypeTable.UTILISATEUR)
                .build();
        passwordRepository.save(newPwd);
        u.setMustChangePassword(false);
        utilisateurRepository.save(u);
        return DataResponse.success("Mot de passe modifie avec succes", null);
    }

    @Override
    @Transactional
    public DataResponse<Void> activerCompte(String verificationCode) {
        throw new UnsupportedOperationException("TODO: implementer activation par code");
    }

    @Override
    @Transactional
    public DataResponse<LoginResponse> login(LoginRequest r) {
        Utilisateur u = utilisateurRepository.findByEmailOrUsername(r.login(), r.login())
                .orElseThrow(() -> new BadRequestException("Identifiants incorrects"));
        if (!u.isActive()) throw new BadRequestException("Compte inactif");
        Passwords pwd = passwordRepository.findActiveByUtilisateurTrackingId(u.getUtilisateurTrackingId());
        if (pwd == null || !passwordEncoder.matches(r.password(), pwd.getPassword()))
            throw new BadRequestException("Identifiants incorrects");

        boolean orgRequires2FA = u.getProfil() != null
                && u.getProfil().getOrganisme() != null
                && u.getProfil().getOrganisme().isTwoFactorEnabled();

        if (orgRequires2FA) {
            var result = twoFactorAuthService.generateAndSendOtp(u);
            return DataResponse.success("Code OTP envoyé",
                    LoginResponse.requires2FA(result.otpTrackingId(), result.maskedEmail()));
        }

        Map<String, Object> tokens = jwtService.generateTokens(u, r.isMobile());
        return DataResponse.success("Connexion reussie", LoginResponse.direct(tokens, buildUserInfo(u)));
    }

    @Override
    @Transactional
    public DataResponse<LoginResponse> verifyOtpAndIssueTokens(UUID otpTrackingId, String code, boolean isMobile) {
        Utilisateur u = twoFactorAuthService.verifyOtp(otpTrackingId, code);
        Map<String, Object> tokens = jwtService.generateTokens(u, isMobile);
        return DataResponse.success("Authentification réussie", LoginResponse.direct(tokens, buildUserInfo(u)));
    }

    @Override
    @Transactional
    public DataResponse<LoginResponse> resendLoginOtp(UUID otpTrackingId) {
        var result = twoFactorAuthService.resendOtp(otpTrackingId);
        return DataResponse.success("Nouveau code envoyé",
                LoginResponse.requires2FA(result.otpTrackingId(), result.maskedEmail()));
    }

    private UserInfoResponse buildUserInfo(Utilisateur u) {
        var org = (u.getProfil() != null) ? u.getProfil().getOrganisme() : null;
        boolean orgTwoFactor = org != null && org.isTwoFactorEnabled();
        return new UserInfoResponse(
                u.getUtilisateurTrackingId(), u.getNom(), u.getPrenoms(), u.getEmail(),
                u.getProfil() != null ? u.getProfil().getProfilNom() : null,
                u.isMustChangePassword(),
                orgTwoFactor,
                org != null ? org.getOrganismeTrackingId() : null,
                org != null ? org.getRaisonSociale() : null);
    }

    @Override
    @Transactional
    public DataResponse<Void> logout(String token) {
        jwtService.revokeToken(token);
        return DataResponse.success("Deconnexion reussie", null);
    }
}
