package com.ossanasur.cbconnect.module.finance.repository;
import com.ossanasur.cbconnect.module.finance.entity.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List; import java.util.Optional; import java.util.UUID;
@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Integer> {
    @Query("SELECT p FROM Paiement p WHERE p.paiementTrackingId=:id AND p.activeData=true AND p.deletedData=false")
    Optional<Paiement> findActiveByTrackingId(@Param("id") UUID id);
    @Query("SELECT p FROM Paiement p WHERE p.sinistre.sinistreTrackingId=:sid AND p.activeData=true AND p.deletedData=false ORDER BY p.dateEmission DESC")
    List<Paiement> findBySinistre(@Param("sid") UUID sinistreId);
}
