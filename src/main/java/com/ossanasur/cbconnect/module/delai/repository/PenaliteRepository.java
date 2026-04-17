package com.ossanasur.cbconnect.module.delai.repository;
import com.ossanasur.cbconnect.module.delai.entity.PenaliteCalculee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List; import java.util.UUID;
@Repository
public interface PenaliteRepository extends JpaRepository<PenaliteCalculee, Integer> {
    @Query("SELECT p FROM PenaliteCalculee p WHERE p.sinistre.sinistreTrackingId=:sid ORDER BY p.dateCalcul DESC")
    List<PenaliteCalculee> findBySinistre(@Param("sid") UUID sinistreId);
}
