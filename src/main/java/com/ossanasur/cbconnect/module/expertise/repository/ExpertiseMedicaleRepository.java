package com.ossanasur.cbconnect.module.expertise.repository;
import com.ossanasur.cbconnect.module.expertise.entity.ExpertiseMedicale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List; import java.util.Optional; import java.util.UUID;
@Repository
public interface ExpertiseMedicaleRepository extends JpaRepository<ExpertiseMedicale, Integer> {
    @Query("SELECT e FROM ExpertiseMedicale e WHERE e.expertiseMedTrackingId=:id AND e.activeData=true AND e.deletedData=false")
    Optional<ExpertiseMedicale> findActiveByTrackingId(@Param("id") UUID id);
    @Query("SELECT e FROM ExpertiseMedicale e WHERE e.victime.victimeTrackingId=:vid AND e.activeData=true AND e.deletedData=false ORDER BY e.dateDemande DESC")
    List<ExpertiseMedicale> findByVictime(@Param("vid") UUID victimeId);
    @Query("SELECT e FROM ExpertiseMedicale e WHERE e.dateRapport IS NULL AND e.activeData=true AND e.deletedData=false ORDER BY e.dateDemande")
    List<ExpertiseMedicale> findSansRapport();
}
