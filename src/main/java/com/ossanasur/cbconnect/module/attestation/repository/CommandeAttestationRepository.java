package com.ossanasur.cbconnect.module.attestation.repository;
import com.ossanasur.cbconnect.module.attestation.entity.CommandeAttestation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional; import java.util.UUID;
@Repository
public interface CommandeAttestationRepository extends JpaRepository<CommandeAttestation, Integer> {
    @Query("SELECT c FROM CommandeAttestation c WHERE c.commandeTrackingId=:id AND c.activeData=true AND c.deletedData=false")
    Optional<CommandeAttestation> findActiveByTrackingId(@Param("id") UUID id);
    @Query("SELECT c FROM CommandeAttestation c WHERE c.organisme.organismeTrackingId=:orgId AND c.activeData=true AND c.deletedData=false ORDER BY c.dateCommande DESC")
    Page<CommandeAttestation> findByOrganisme(@Param("orgId") UUID orgId, Pageable pageable);
    @Query("SELECT c FROM CommandeAttestation c WHERE c.activeData=true AND c.deletedData=false ORDER BY c.dateCommande DESC")
    Page<CommandeAttestation> findAllActive(Pageable pageable);
    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(c.numeroCommande, 10) AS int)),0) FROM CommandeAttestation c WHERE c.numeroCommande LIKE CONCAT('CMD-', :annee, '-%')")
    long findMaxSequenceByAnnee(@Param("annee") int annee);
}
