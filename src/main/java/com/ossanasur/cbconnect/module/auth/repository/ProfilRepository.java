package com.ossanasur.cbconnect.module.auth.repository;
import com.ossanasur.cbconnect.module.auth.entity.Profil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfilRepository extends JpaRepository<Profil, Integer> {
    @Query("SELECT p FROM Profil p WHERE p.profilTrackingId = :id AND p.activeData = true AND p.deletedData = false")
    Optional<Profil> findActiveByTrackingId(@Param("id") UUID id);
    @Query("SELECT p FROM Profil p WHERE p.activeData = true AND p.deletedData = false ORDER BY p.createdAt DESC")
    List<Profil> findAllActive();
    @Query("SELECT p FROM Profil p WHERE p.organisme.organismeTrackingId = :orgId AND p.activeData = true AND p.deletedData = false")
    List<Profil> findAllActiveByOrganisme(@Param("orgId") UUID orgId);
}
