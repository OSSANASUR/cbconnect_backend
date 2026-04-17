package com.ossanasur.cbconnect.module.indemnisation.repository;
import com.ossanasur.cbconnect.module.indemnisation.entity.AyantDroit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List; import java.util.Optional; import java.util.UUID;
@Repository
public interface AyantDroitRepository extends JpaRepository<AyantDroit, Integer> {
    @Query("SELECT a FROM AyantDroit a WHERE a.ayantDroitTrackingId=:id AND a.activeData=true AND a.deletedData=false")
    Optional<AyantDroit> findActiveByTrackingId(@Param("id") UUID id);
    @Query("SELECT a FROM AyantDroit a WHERE a.victime.victimeTrackingId=:vid AND a.activeData=true AND a.deletedData=false ORDER BY a.lien, a.nom")
    List<AyantDroit> findByVictime(@Param("vid") UUID victimeId);
}
