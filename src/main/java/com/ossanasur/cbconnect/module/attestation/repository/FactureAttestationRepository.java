package com.ossanasur.cbconnect.module.attestation.repository;
import com.ossanasur.cbconnect.module.attestation.entity.FactureAttestation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List; import java.util.Optional; import java.util.UUID;
@Repository
public interface FactureAttestationRepository extends JpaRepository<FactureAttestation, Integer> {
    @Query("SELECT f FROM FactureAttestation f WHERE f.factureTrackingId=:id AND f.activeData=true AND f.deletedData=false")
    Optional<FactureAttestation> findActiveByTrackingId(@Param("id") UUID id);
    @Query("SELECT f FROM FactureAttestation f WHERE f.activeData=true AND f.deletedData=false ORDER BY f.dateFacture DESC")
    Page<FactureAttestation> findAllActive(Pageable pageable);
    @Query("SELECT f FROM FactureAttestation f WHERE f.commande.commandeTrackingId=:cmdId AND f.activeData=true AND f.deletedData=false ORDER BY f.dateFacture DESC")
    List<FactureAttestation> findByCommande(@Param("cmdId") UUID cmdId);
    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(f.numeroFacture, 6, 3) AS int)),0) FROM FactureAttestation f WHERE f.numeroFacture LIKE CONCAT('BNCB-%/', :annee)")
    long findMaxSequenceByAnnee(@Param("annee") int annee);
}
