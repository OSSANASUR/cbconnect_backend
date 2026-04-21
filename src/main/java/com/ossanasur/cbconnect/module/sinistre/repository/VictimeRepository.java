package com.ossanasur.cbconnect.module.sinistre.repository;

import com.ossanasur.cbconnect.common.enums.StatutVictime;
import com.ossanasur.cbconnect.module.sinistre.entity.Victime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VictimeRepository extends JpaRepository<Victime, Integer> {
    @Query("SELECT v FROM Victime v WHERE v.victimeTrackingId=:id AND v.activeData=true AND v.deletedData=false")
    Optional<Victime> findActiveByTrackingId(@Param("id") UUID id);

    @Query("SELECT v FROM Victime v WHERE v.sinistre.sinistreTrackingId=:sid AND v.activeData=true AND v.deletedData=false ORDER BY v.nom")
    List<Victime> findAllBySinistre(@Param("sid") UUID sinistreTrackingId);

    @Query("SELECT v FROM Victime v WHERE v.sinistre.sinistreTrackingId=:sid AND v.statutVictime=:statut AND v.activeData=true AND v.deletedData=false")
    List<Victime> findBySinistreAndStatut(@Param("sid") UUID sinistreId, @Param("statut") StatutVictime statut);
}
