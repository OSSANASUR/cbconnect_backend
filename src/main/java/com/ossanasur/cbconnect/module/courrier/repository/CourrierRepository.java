package com.ossanasur.cbconnect.module.courrier.repository;
import com.ossanasur.cbconnect.module.courrier.entity.Courrier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List; import java.util.Optional; import java.util.UUID;
@Repository
public interface CourrierRepository extends JpaRepository<Courrier, Integer> {
    @Query("SELECT c FROM Courrier c WHERE c.courrierTrackingId=:id AND c.activeData=true AND c.deletedData=false")
    Optional<Courrier> findActiveByTrackingId(@Param("id") UUID id);
    @Query("SELECT c FROM Courrier c WHERE c.sinistre.sinistreTrackingId=:sid AND c.activeData=true AND c.deletedData=false ORDER BY c.dateCourrier DESC")
    List<Courrier> findBySinistre(@Param("sid") UUID sinistreId);
    @Query("SELECT c FROM Courrier c WHERE c.traite=false AND c.activeData=true AND c.deletedData=false ORDER BY c.dateCourrier ASC")
    List<Courrier> findNonTraites();
}
