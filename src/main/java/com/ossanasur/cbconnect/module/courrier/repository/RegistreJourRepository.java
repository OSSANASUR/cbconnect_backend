package com.ossanasur.cbconnect.module.courrier.repository;

import com.ossanasur.cbconnect.common.enums.StatutRegistre;
import com.ossanasur.cbconnect.common.enums.TypeRegistre;
import com.ossanasur.cbconnect.module.courrier.entity.RegistreJour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RegistreJourRepository extends JpaRepository<RegistreJour, Integer> {

    @Query("SELECT r FROM RegistreJour r WHERE r.registreTrackingId = :id AND r.activeData = true AND r.deletedData = false")
    Optional<RegistreJour> findActiveByTrackingId(@Param("id") UUID id);

    @Query("SELECT r FROM RegistreJour r WHERE r.dateJour = :date AND r.typeRegistre = :type AND r.activeData = true AND r.deletedData = false")
    Optional<RegistreJour> findByDateAndType(@Param("date") LocalDate date, @Param("type") TypeRegistre type);

    @Query("SELECT r FROM RegistreJour r WHERE r.activeData = true AND r.deletedData = false ORDER BY r.dateJour DESC")
    List<RegistreJour> findAllActive();

    @Query("SELECT r FROM RegistreJour r WHERE r.statut = :statut AND r.activeData = true AND r.deletedData = false ORDER BY r.dateJour DESC")
    List<RegistreJour> findByStatut(@Param("statut") StatutRegistre statut);
}
