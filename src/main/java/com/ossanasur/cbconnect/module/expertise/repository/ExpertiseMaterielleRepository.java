package com.ossanasur.cbconnect.module.expertise.repository;

import com.ossanasur.cbconnect.module.expertise.entity.ExpertiseMaterielle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExpertiseMaterielleRepository extends JpaRepository<ExpertiseMaterielle, Integer> {

    @Query("SELECT e FROM ExpertiseMaterielle e WHERE e.expertiseMaTrackingId=:id AND e.activeData=true AND e.deletedData=false")
    Optional<ExpertiseMaterielle> findActiveByTrackingId(@Param("id") UUID id);

    @Query("SELECT e FROM ExpertiseMaterielle e WHERE e.victime.victimeTrackingId=:vid AND e.activeData=true AND e.deletedData=false ORDER BY e.dateDemande DESC")
    List<ExpertiseMaterielle> findByVictime(@Param("vid") UUID victimeId);

    @Query("SELECT e FROM ExpertiseMaterielle e WHERE e.sinistre.sinistreTrackingId=:sid AND e.activeData=true AND e.deletedData=false ORDER BY e.dateDemande DESC")
    List<ExpertiseMaterielle> findBySinistre(@Param("sid") UUID sinistreId);

    @Query("SELECT e FROM ExpertiseMaterielle e WHERE e.dateRapport IS NULL AND e.activeData=true AND e.deletedData=false")
    List<ExpertiseMaterielle> findSansRapport();
}