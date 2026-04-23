package com.ossanasur.cbconnect.security.repository;

import com.ossanasur.cbconnect.security.entity.Passwords;

import jakarta.persistence.criteria.CriteriaBuilder.In;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PasswordRepository extends JpaRepository<Passwords, Integer> {

    @Query("SELECT p FROM Passwords p WHERE p.utilisateur.utilisateurTrackingId = :uid AND p.activeData = true AND p.deletedData = false")

    Passwords findActiveByUtilisateurTrackingId(@Param("uid") UUID uid);

    @Query(value = """
            SELECT *
            FROM passwords
            WHERE utilisateur_id = :utilisateurId
            ORDER BY created_at DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Passwords> findNLastPasswords(
            @Param("utilisateurId") Integer utilisateurId,
            @Param("limit") int limit);
}
