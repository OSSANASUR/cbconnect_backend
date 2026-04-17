package com.ossanasur.cbconnect.module.finance.repository;
import com.ossanasur.cbconnect.module.finance.entity.Encaissement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List; import java.util.Optional; import java.util.UUID;
@Repository
public interface EncaissementRepository extends JpaRepository<Encaissement, Integer> {
    @Query("SELECT e FROM Encaissement e WHERE e.encaissementTrackingId=:id AND e.activeData=true AND e.deletedData=false")
    Optional<Encaissement> findActiveByTrackingId(@Param("id") UUID id);
    @Query("SELECT e FROM Encaissement e WHERE e.sinistre.sinistreTrackingId=:sid AND e.activeData=true AND e.deletedData=false ORDER BY e.dateReception DESC")
    List<Encaissement> findBySinistre(@Param("sid") UUID sinistreId);
}
