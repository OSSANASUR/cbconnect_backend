package com.ossanasur.cbconnect.module.attestation.repository;
import com.ossanasur.cbconnect.module.attestation.entity.ChequeRecuAttestation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List; import java.util.Optional; import java.util.UUID;
@Repository
public interface ChequeRecuAttestationRepository extends JpaRepository<ChequeRecuAttestation, Integer> {
    @Query("SELECT c FROM ChequeRecuAttestation c WHERE c.chequeTrackingId=:id AND c.activeData=true AND c.deletedData=false")
    Optional<ChequeRecuAttestation> findActiveByTrackingId(@Param("id") UUID id);
    @Query("SELECT c FROM ChequeRecuAttestation c WHERE c.activeData=true AND c.deletedData=false ORDER BY c.dateReception DESC, c.dateEmission DESC")
    Page<ChequeRecuAttestation> findAllActive(Pageable pageable);
    @Query("SELECT c FROM ChequeRecuAttestation c WHERE c.facture.commande.commandeTrackingId=:cmdId AND c.activeData=true AND c.deletedData=false ORDER BY c.dateEmission DESC")
    List<ChequeRecuAttestation> findByCommande(@Param("cmdId") UUID cmdId);
}
