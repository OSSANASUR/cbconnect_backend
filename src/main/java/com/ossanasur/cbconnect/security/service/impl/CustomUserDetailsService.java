package com.ossanasur.cbconnect.security.service.impl;

import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.module.auth.repository.UtilisateurRepository;
import com.ossanasur.cbconnect.security.entity.Passwords;
import com.ossanasur.cbconnect.security.repository.PasswordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordRepository passwordRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Utilisateur u = utilisateurRepository.findByEmailOrUsername(login, login)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + login));
        if (!u.isActive()) throw new UsernameNotFoundException("Compte inactif");
        Passwords passwords = passwordRepository.findActiveByUtilisateurTrackingId(u.getUtilisateurTrackingId());
        if (passwords == null) throw new UsernameNotFoundException("Aucun mot de passe actif pour : " + login);
        return User.builder()
                .username(u.getEmail())
                .password(passwords.getPassword())
                .authorities(getAuthorities(u))
                .disabled(!u.isActive())
                .accountExpired(false).accountLocked(false).credentialsExpired(false)
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Utilisateur u) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        // Role de base
        if (u.getProfil() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + u.getProfil().getProfilNom().toUpperCase()));
            if (u.getProfil().getHabilitations() != null) {
                u.getProfil().getHabilitations().forEach(h ->
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + h.getCodeHabilitation().toUpperCase())));
            }
        }
        return authorities;
    }
}
