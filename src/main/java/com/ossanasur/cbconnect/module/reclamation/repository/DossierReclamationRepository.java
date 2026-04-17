package com.ossanasur.cbconnect.module.reclamation.repository;
import com.ossanasur.cbconnect.module.reclamation.entity.DossierReclamation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal; import java.util.List; import java.util.Optional; import java.util.UUID;
@Repository
public interface DossierReclamationRepository extends JpaRepository<DossierReclamation, Integer> {
    @Query("SELECT d FROM DossierReclamation d WHERE d.dossierTrackingId=:id AND d.activeData=true AND d.deletedData=false")
    Optional<DossierReclamation> findActiveByTrackingId(@Param("id") UUID id);
    @Query("SELECT d FROM DossierReclamation d WHERE d.victime.victimeTrackingId=:vid AND d.activeData=true AND d.deletedData=false ORDER BY d.dateOuverture DESC")
    List<DossierReclamation> findByVictime(@Param("vid") UUID victimeId);
    @Query("SELECT COALESCE(SUM(d.montantTotalRetenu),0) FROM DossierReclamation d WHERE d.victime.victimeTrackingId=:vid AND d.activeData=true AND d.deletedData=false")
    Optional<BigDecimal> findMontantRetenuByVictime(@Param("vid") UUID victimeId);
}
