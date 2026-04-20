package com.ossanasur.cbconnect.security.repository;

import com.ossanasur.cbconnect.security.entity.Habilitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HabilitationRepository extends JpaRepository<Habilitation, Integer> {

    @Query("SELECT h FROM Habilitation h WHERE h.habilitationTrackingId = :id AND h.activeData = true AND h.deletedData = false")
    Optional<Habilitation> findActiveByTrackingId(@Param("id") UUID id);

    @Query("SELECT h FROM Habilitation h WHERE h.activeData = true AND h.deletedData = false ORDER BY h.moduleEntity.nomModule, h.codeHabilitation")
    List<Habilitation> findAllActive();

    @Query("SELECT h FROM Habilitation h WHERE h.codeHabilitation = :code AND h.activeData = true AND h.deletedData = false")
    Optional<Habilitation> findActiveByCode(@Param("code") String code);

    @Query("SELECT h FROM Habilitation h WHERE h.moduleEntity.moduleTrackingId = :moduleId AND h.activeData = true AND h.deletedData = false ORDER BY h.codeHabilitation")
    List<Habilitation> findActiveByModule(@Param("moduleId") UUID moduleId);
}
