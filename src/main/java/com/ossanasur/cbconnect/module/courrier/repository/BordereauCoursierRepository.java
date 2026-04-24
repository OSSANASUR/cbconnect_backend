package com.ossanasur.cbconnect.module.courrier.repository;

import com.ossanasur.cbconnect.common.enums.StatutBordereau;
import com.ossanasur.cbconnect.module.courrier.entity.BordereauCoursier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BordereauCoursierRepository extends JpaRepository<BordereauCoursier, Integer> {

    @Query("SELECT b FROM BordereauCoursier b WHERE b.bordereauTrackingId = :id AND b.activeData = true AND b.deletedData = false")
    Optional<BordereauCoursier> findActiveByTrackingId(@Param("id") UUID id);

    @Query("SELECT b FROM BordereauCoursier b WHERE b.activeData = true AND b.deletedData = false ORDER BY b.dateCreation DESC")
    List<BordereauCoursier> findAllActive();

    @Query("SELECT b FROM BordereauCoursier b WHERE b.statut = :statut AND b.activeData = true AND b.deletedData = false ORDER BY b.dateCreation DESC")
    List<BordereauCoursier> findByStatut(@Param("statut") StatutBordereau statut);

    @Query("SELECT COUNT(b) FROM BordereauCoursier b WHERE EXTRACT(YEAR FROM b.dateCreation) = :annee")
    long countForYear(@Param("annee") int annee);

    boolean existsByNumeroBordereauAndActiveDataTrueAndDeletedDataFalse(String numero);
}
