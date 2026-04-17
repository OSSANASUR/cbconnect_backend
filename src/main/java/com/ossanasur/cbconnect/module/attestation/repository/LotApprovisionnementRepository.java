package com.ossanasur.cbconnect.module.attestation.repository;
import com.ossanasur.cbconnect.module.attestation.entity.LotApprovisionnement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List; import java.util.Optional; import java.util.UUID;
@Repository
public interface LotApprovisionnementRepository extends JpaRepository<LotApprovisionnement, Integer> {
    @Query("SELECT l FROM LotApprovisionnement l WHERE l.lotTrackingId=:id AND l.activeData=true AND l.deletedData=false")
    Optional<LotApprovisionnement> findActiveByTrackingId(@Param("id") UUID id);
    @Query("SELECT l FROM LotApprovisionnement l WHERE l.activeData=true AND l.deletedData=false ORDER BY l.dateCommande DESC")
    List<LotApprovisionnement> findAllActive();
}
