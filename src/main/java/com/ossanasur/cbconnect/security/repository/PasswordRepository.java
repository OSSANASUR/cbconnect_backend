package com.ossanasur.cbconnect.security.repository;
import com.ossanasur.cbconnect.security.entity.Passwords;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface PasswordRepository extends JpaRepository<Passwords, Integer> {
    @Query("SELECT p FROM Passwords p WHERE p.utilisateur.utilisateurTrackingId = :uid AND p.activeData = true AND p.deletedData = false")
    Passwords findActiveByUtilisateurTrackingId(@Param("uid") UUID uid);
}
