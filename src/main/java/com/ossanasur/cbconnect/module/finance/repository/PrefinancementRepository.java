package com.ossanasur.cbconnect.module.finance.repository;
import com.ossanasur.cbconnect.module.finance.entity.Prefinancement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional; import java.util.UUID;
@Repository
public interface PrefinancementRepository extends JpaRepository<Prefinancement, Integer> {
    @Query("SELECT p FROM Prefinancement p WHERE p.sinistre.sinistreTrackingId=:sid AND p.activeData=true AND p.deletedData=false")
    Optional<Prefinancement> findBySinistre(@Param("sid") UUID sinistreId);
}
