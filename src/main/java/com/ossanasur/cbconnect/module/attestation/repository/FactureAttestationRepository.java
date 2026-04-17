package com.ossanasur.cbconnect.module.attestation.repository;
import com.ossanasur.cbconnect.module.attestation.entity.FactureAttestation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional; import java.util.UUID;
@Repository
public interface FactureAttestationRepository extends JpaRepository<FactureAttestation, Integer> {
    @Query("SELECT f FROM FactureAttestation f WHERE f.factureTrackingId=:id AND f.activeData=true AND f.deletedData=false")
    Optional<FactureAttestation> findActiveByTrackingId(@Param("id") UUID id);
    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(f.numeroFacture, 6, 3) AS int)),0) FROM FactureAttestation f WHERE f.numeroFacture LIKE CONCAT('BNCB-%/', :annee)")
    long findMaxSequenceByAnnee(@Param("annee") int annee);
}
