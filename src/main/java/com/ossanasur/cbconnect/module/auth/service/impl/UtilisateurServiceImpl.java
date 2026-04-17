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
import com.ossanasur.cbconnect.module.auth.service.UtilisateurService;
import com.ossanasur.cbconnect.security.dto.request.ChangePasswordRequest;
import com.ossanasur.cbconnect.security.dto.request.LoginRequest;
import com.ossanasur.cbconnect.security.dto.response.LoginResponse;
import com.ossanasur.cbconnect.security.dto.response.UserInfoResponse;
import com.ossanasur.cbconnect.security.entity.Passwords;
import com.ossanasur.cbconnect.security.repository.PasswordRepository;
import com.ossanasur.cbconnect.security.service.JwtService;
import com.ossanasur.cbconnect.utils.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public DataResponse<UtilisateurResponse> creer(UtilisateurRequest r, String password, String loginAuteur) {
        if (utilisateurRepository.existsByEmailAndActiveDataTrueAndDeletedDataFalse(r.email()))
            throw new AlreadyExistException("L'email '" + r.email() + "' est deja utilise");

        Profil profil = null;
        if (r.profilTrackingId() != null) {
            profil = profilRepository.findActiveByTrackingId(r.profilTrackingId())
                    .orElseThrow(() -> new RessourceNotFoundException("Profil introuvable"));
        }

        Utilisateur u = Utilisateur.builder()
                .utilisateurTrackingId(UUID.randomUUID())
                .nom(r.nom()).prenoms(r.prenoms()).email(r.email())
                .username(r.username() != null ? r.username() : r.email())
                .telephone(r.telephone()).profil(profil)
                .isActive(true).mustChangePassword(true)
                .createdBy(loginAuteur).activeData(true).deletedData(false)
                .fromTable(com.ossanasur.cbconnect.common.enums.TypeTable.UTILISATEUR)
                .build();
        Utilisateur saved = utilisateurRepository.save(u);

        Passwords pwd = Passwords.builder()
                .passwordsTrackingId(UUID.randomUUID())
                .password(passwordEncoder.encode(password))
                .isTemporary(true).utilisateur(saved)
                .createdBy(loginAuteur).activeData(true).deletedData(false)
                .fromTable(com.ossanasur.cbconnect.common.enums.TypeTable.UTILISATEUR)
                .build();
        passwordRepository.save(pwd);

        return DataResponse.created("Utilisateur cree avec succes", utilisateurMapper.toResponse(saved));
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
        Map<String, Object> tokens = jwtService.generateTokens(u, r.isMobile());
        UserInfoResponse info = new UserInfoResponse(
                u.getUtilisateurTrackingId(), u.getNom(), u.getPrenoms(), u.getEmail(),
                u.getProfil() != null ? u.getProfil().getProfilNom() : null, u.isMustChangePassword());
        return DataResponse.success("Connexion reussie", new LoginResponse(tokens, info));
    }

    @Override
    @Transactional
    public DataResponse<Void> logout(String token) {
        jwtService.revokeToken(token);
        return DataResponse.success("Deconnexion reussie", null);
    }
}
