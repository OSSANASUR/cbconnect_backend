package com.ossanasur.cbconnect.module.ged.repository;

import com.ossanasur.cbconnect.module.ged.entity.PaperlessDossier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaperlessDossierRepository extends JpaRepository<PaperlessDossier, Integer> {
    @Query("SELECT d FROM PaperlessDossier d WHERE d.sinistre.sinistreTrackingId=:sid AND d.victime IS NULL AND d.activeData=true AND d.deletedData=false")
    Optional<PaperlessDossier> findRootBySinistre(@Param("sid") UUID sinistreId);

    @Query("SELECT d FROM PaperlessDossier d WHERE d.victime.victimeTrackingId=:vid AND d.activeData=true AND d.deletedData=false")
    Optional<PaperlessDossier> findByVictime(@Param("vid") UUID victimeId);
}
