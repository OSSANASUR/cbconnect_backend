package com.ossanasur.cbconnect.module.auth.repository;

import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {
    @Query("SELECT u FROM Utilisateur u WHERE (u.email = :login OR u.username = :login) AND u.activeData = true AND u.deletedData = false")
    Optional<Utilisateur> findByEmailOrUsername(@Param("login") String login, @Param("login2") String login2);

    Optional<Utilisateur> findByEmailAndActiveDataTrueAndDeletedDataFalse(String email);

    Optional<Utilisateur> findByUtilisateurTrackingIdAndActiveDataTrueAndDeletedDataFalse(UUID trackingId);

    @Query("SELECT u FROM Utilisateur u WHERE u.activeData = true AND u.deletedData = false ORDER BY u.createdAt DESC")
    List<Utilisateur> findAllActive();

    @Query("SELECT u FROM Utilisateur u WHERE u.utilisateurTrackingId = :id ORDER BY u.createdAt DESC")
    List<Utilisateur> findHistoryByTrackingId(@Param("id") UUID id);

    boolean existsByEmailAndActiveDataTrueAndDeletedDataFalse(String email);

    // findByUsername
    Optional<Utilisateur> findByUsernameAndActiveDataTrueAndDeletedDataFalse(String username);
}
