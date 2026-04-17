package com.ossanasur.cbconnect.module.sinistre.repository;
import com.ossanasur.cbconnect.module.sinistre.entity.EntiteConstat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List; import java.util.Optional; import java.util.UUID;
@Repository
public interface EntiteConstatRepository extends JpaRepository<EntiteConstat, Integer> {
    @Query("SELECT e FROM EntiteConstat e WHERE e.entiteConstatTrackingId=:id AND e.activeData=true AND e.deletedData=false")
    Optional<EntiteConstat> findActiveByTrackingId(@Param("id") UUID id);
    @Query("SELECT e FROM EntiteConstat e WHERE e.activeData=true AND e.deletedData=false AND e.actif=true ORDER BY e.nom")
    List<EntiteConstat> findAllActifs();
}
