package com.ossanasur.cbconnect.module.attestation.repository;
import com.ossanasur.cbconnect.module.attestation.entity.ChequeRecuAttestation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional; import java.util.UUID;
@Repository
public interface ChequeRecuAttestationRepository extends JpaRepository<ChequeRecuAttestation, Integer> {
    @Query("SELECT c FROM ChequeRecuAttestation c WHERE c.chequeTrackingId=:id AND c.activeData=true AND c.deletedData=false")
    Optional<ChequeRecuAttestation> findActiveByTrackingId(@Param("id") UUID id);
}
