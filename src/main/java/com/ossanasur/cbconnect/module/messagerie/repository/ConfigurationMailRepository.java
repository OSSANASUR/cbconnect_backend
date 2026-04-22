package com.ossanasur.cbconnect.module.messagerie.repository;

import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.module.messagerie.entity.ConfigurationMail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConfigurationMailRepository extends JpaRepository<ConfigurationMail, Integer> {

    Optional<ConfigurationMail> findByUtilisateur(Utilisateur utilisateur);

    Optional<ConfigurationMail> findByTrackingId(UUID trackingId);
}