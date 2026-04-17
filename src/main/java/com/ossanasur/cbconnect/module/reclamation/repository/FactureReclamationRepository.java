package com.ossanasur.cbconnect.module.reclamation.repository;
import com.ossanasur.cbconnect.module.reclamation.entity.FactureReclamation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List; import java.util.Optional; import java.util.UUID;
@Repository
public interface FactureReclamationRepository extends JpaRepository<FactureReclamation, Integer> {
    @Query("SELECT f FROM FactureReclamation f WHERE f.factureTrackingId=:id AND f.activeData=true AND f.deletedData=false")
    Optional<FactureReclamation> findActiveByTrackingId(@Param("id") UUID id);
    @Query("SELECT f FROM FactureReclamation f WHERE f.dossierReclamation.dossierTrackingId=:did AND f.activeData=true AND f.deletedData=false ORDER BY f.dateFacture")
    List<FactureReclamation> findByDossier(@Param("did") UUID dossierId);
}
