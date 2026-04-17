package com.ossanasur.cbconnect.module.indemnisation.repository;
import com.ossanasur.cbconnect.module.indemnisation.entity.OffreIndemnisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional; import java.util.UUID;
@Repository
public interface OffreIndemnisationRepository extends JpaRepository<OffreIndemnisation, Integer> {
    @Query("SELECT o FROM OffreIndemnisation o WHERE o.offreTrackingId=:id AND o.activeData=true AND o.deletedData=false")
    Optional<OffreIndemnisation> findActiveByTrackingId(@Param("id") UUID id);
    @Query("SELECT o FROM OffreIndemnisation o WHERE o.victime.victimeTrackingId=:vid AND o.activeData=true AND o.deletedData=false ORDER BY o.createdAt DESC")
    Optional<OffreIndemnisation> findLastByVictime(@Param("vid") UUID victimeId);
}
